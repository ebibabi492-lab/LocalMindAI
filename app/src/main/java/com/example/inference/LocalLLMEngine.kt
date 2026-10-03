package com.example.inference

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.example.model.ModelInferenceStatus
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class LocalLLMEngine(private val context: Context) {

    private val tag = "LocalMind-LLM"

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private val mutex = Mutex()

    private val _status = MutableStateFlow(ModelInferenceStatus.NO_MODEL_SELECTED)
    val status: StateFlow<ModelInferenceStatus> = _status.asStateFlow()

    private val _loadedModelPath = MutableStateFlow<String?>(null)
    val loadedModelPath: StateFlow<String?> = _loadedModelPath.asStateFlow()

    private val _activeBackend = MutableStateFlow<String>("None")
    val activeBackend: StateFlow<String> = _activeBackend.asStateFlow()

    private val _statusMessage = MutableStateFlow<String>("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var lastTemperature: Float = 0.7f
    private var lastMaxTokens: Int = 1024

    fun getAvailableRamMb(): Long {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            memInfo.availMem / (1024 * 1024)
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Checks if OpenCL library is physically available on this Android device.
     */
    fun isOpenClAvailable(): Boolean {
        val paths = listOf(
            "/system/vendor/lib64/libOpenCL.so",
            "/vendor/lib64/libOpenCL.so",
            "/system/lib64/libOpenCL.so",
            "/system/vendor/lib/libOpenCL.so",
            "/vendor/lib/libOpenCL.so",
            "/system/lib/libOpenCL.so"
        )
        val fileFound = paths.any { File(it).exists() }
        if (!fileFound) {
            return false
        }
        return try {
            System.loadLibrary("OpenCL")
            true
        } catch (e: UnsatisfiedLinkError) {
            fileFound
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun loadModel(
        modelFilePath: String,
        preferredBackend: String = "cpu",
        temperature: Float = 0.7f,
        maxTokens: Int = 1024
    ): Result<Unit> = withContext(Dispatchers.Default) {
        mutex.withLock {
            lastTemperature = temperature
            lastMaxTokens = maxTokens

            val file = File(modelFilePath)
            if (!file.exists() || file.length() < 1024 * 1024) {
                _status.value = ModelInferenceStatus.ERROR
                _statusMessage.value = "Model file does not exist or is incomplete"
                return@withContext Result.failure(Exception("Model file does not exist"))
            }

            _status.value = ModelInferenceStatus.LOADING
            _statusMessage.value = "Allocating local memory and loading model..."

            // Release any previously loaded engine
            releaseResourcesInternal()

            val cacheDir = File(context.cacheDir, "litert_cache").apply { mkdirs() }.absolutePath

            var loadedSuccessfully = false
            var usedBackendName = "CPU"
            var lastError: Throwable? = null

            val hasOpenCl = isOpenClAvailable()
            Log.d(tag, "Checking OpenCL availability on device: $hasOpenCl")

            // Determine backend attempt order based on device hardware support
            val backendsToTry = when (preferredBackend.lowercase()) {
                "gpu" -> {
                    if (hasOpenCl) {
                        listOf(Backend.GPU() to "GPU", Backend.CPU() to "CPU (Fallback)")
                    } else {
                        Log.i(tag, "OpenCL is not present on this device. Forcing CPU backend.")
                        listOf(Backend.CPU() to "CPU (OpenCL unavailable)")
                    }
                }
                "auto" -> {
                    if (hasOpenCl) {
                        listOf(Backend.GPU() to "GPU", Backend.CPU() to "CPU (Fallback)")
                    } else {
                        listOf(Backend.CPU() to "CPU")
                    }
                }
                else -> listOf(Backend.CPU() to "CPU") // Default and stable for all Android hardware
            }

            for ((backend, name) in backendsToTry) {
                try {
                    Log.d(tag, "Attempting to initialize LiteRT-LM Engine with backend: $name for model $modelFilePath")
                    _statusMessage.value = "Initializing engine with $name..."

                    val config = EngineConfig(
                        modelPath = modelFilePath,
                        backend = backend,
                        cacheDir = cacheDir
                    )

                    val newEngine = Engine(config)
                    newEngine.initialize()

                    // Configure conversation session
                    val systemPrompt = "You are LocalMind AI, a helpful, polite, and intelligent offline AI assistant running locally on the user's phone. You provide clear, concise, and helpful answers in both Persian and English based on the language of the prompt."
                    val sampler = SamplerConfig(
                        topK = 40,
                        topP = 0.95,
                        temperature = temperature.toDouble().coerceIn(0.1, 1.5),
                        seed = 0
                    )
                    val convConfig = ConversationConfig(
                        systemInstruction = Contents.of(systemPrompt),
                        samplerConfig = sampler
                    )

                    val newConv = newEngine.createConversation(convConfig)

                    // If GPU was attempted, probe test 1 token to catch OpenCL runtime failure before user queries
                    if (backend is Backend.GPU) {
                        Log.d(tag, "Probing GPU execution to verify OpenCL driver compatibility...")
                        try {
                            val probeFlow = newConv.sendMessageAsync("test")
                            withTimeoutOrNull(2500) {
                                probeFlow.collect {
                                    throw CancellationException("Probe finished successfully")
                                }
                            }
                        } catch (e: CancellationException) {
                            // Normal exit from probe
                            Log.d(tag, "GPU probe succeeded.")
                        } catch (probeError: Throwable) {
                            val msg = probeError.message ?: ""
                            Log.w(tag, "GPU probe failed: $msg")
                            if (msg.contains("opencl", ignoreCase = true) ||
                                msg.contains("status code:2", ignoreCase = true) ||
                                msg.contains("can not find", ignoreCase = true)
                            ) {
                                throw Exception("OpenCL library is not supported on this device GPU: $msg", probeError)
                            }
                        }
                    }

                    engine = newEngine
                    conversation = newConv
                    _loadedModelPath.value = modelFilePath
                    usedBackendName = name
                    loadedSuccessfully = true
                    Log.d(tag, "Engine successfully initialized and verified with $name")
                    break
                } catch (t: Throwable) {
                    Log.w(tag, "Failed to initialize with backend $name: ${t.message}", t)
                    lastError = t
                    releaseResourcesInternal()
                }
            }

            if (loadedSuccessfully) {
                _status.value = ModelInferenceStatus.READY_OFFLINE
                _activeBackend.value = usedBackendName
                _statusMessage.value = "Offline AI Ready ($usedBackendName)"
                Result.success(Unit)
            } else {
                _status.value = ModelInferenceStatus.ERROR
                val errMsg = lastError?.localizedMessage ?: "Failed to initialize LiteRT-LM engine"
                _statusMessage.value = "Initialization error: $errMsg"
                Result.failure(lastError ?: Exception(errMsg))
            }
        }
    }

    fun generateStreamingResponse(prompt: String): Flow<String> = flow {
        val conv = conversation
        if (conv == null || _status.value != ModelInferenceStatus.READY_OFFLINE) {
            emit("Error: No offline AI model is currently loaded.")
            return@flow
        }

        try {
            var fullResponse = ""
            conv.sendMessageAsync(prompt).collect { message ->
                val chunk = message.contents.contents
                    .filterIsInstance<Content.Text>()
                    .joinToString("") { it.text }

                if (chunk.isNotEmpty()) {
                    fullResponse += chunk
                    emit(fullResponse)
                }
            }
        } catch (e: CancellationException) {
            Log.d(tag, "Generation cancelled by user")
            throw e
        } catch (t: Throwable) {
            val errMsg = t.localizedMessage ?: t.message ?: "Unknown inference error"
            Log.e(tag, "Error during inference: $errMsg", t)

            // If an OpenCL / GPU error occurs during query, automatically fallback to CPU
            if (errMsg.contains("opencl", ignoreCase = true) ||
                errMsg.contains("status code:2", ignoreCase = true) ||
                errMsg.contains("can not find", ignoreCase = true)
            ) {
                val currentPath = _loadedModelPath.value
                if (currentPath != null) {
                    emit("\n[Switching automatically to CPU backend due to device GPU/OpenCL limitation...]\n")
                    try {
                        loadModel(
                            modelFilePath = currentPath,
                            preferredBackend = "cpu",
                            temperature = lastTemperature,
                            maxTokens = lastMaxTokens
                        )
                        val cpuConv = conversation
                        if (cpuConv != null) {
                            var cpuResponse = ""
                            cpuConv.sendMessageAsync(prompt).collect { message ->
                                val chunk = message.contents.contents
                                    .filterIsInstance<Content.Text>()
                                    .joinToString("") { it.text }
                                if (chunk.isNotEmpty()) {
                                    cpuResponse += chunk
                                    emit(cpuResponse)
                                }
                            }
                            return@flow
                        }
                    } catch (cpuError: Throwable) {
                        Log.e(tag, "CPU fallback also encountered error: ${cpuError.message}", cpuError)
                    }
                }
            }

            emit("\n[Inference error: $errMsg]")
        }
    }.flowOn(Dispatchers.Default)

    fun startNewConversation(temperature: Float = 0.7f) {
        val eng = engine ?: return
        try {
            val systemPrompt = "You are LocalMind AI, a helpful, polite, and intelligent offline AI assistant running locally on the user's phone. You provide clear, concise, and helpful answers in both Persian and English based on the language of the prompt."
            val sampler = SamplerConfig(
                topK = 40,
                topP = 0.95,
                temperature = temperature.toDouble().coerceIn(0.1, 1.5),
                seed = 0
            )
            val convConfig = ConversationConfig(
                systemInstruction = Contents.of(systemPrompt),
                samplerConfig = sampler
            )
            conversation = eng.createConversation(convConfig)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start new conversation: ${e.message}", e)
        }
    }

    private fun releaseResourcesInternal() {
        try {
            conversation = null
            engine?.close()
            engine = null
        } catch (e: Exception) {
            Log.w(tag, "Error closing previous engine: ${e.message}")
        }
    }

    suspend fun unloadModel() = withContext(Dispatchers.Default) {
        mutex.withLock {
            releaseResourcesInternal()
            _status.value = ModelInferenceStatus.NO_MODEL_SELECTED
            _loadedModelPath.value = null
            _activeBackend.value = "None"
            _statusMessage.value = "No model loaded"
        }
    }
}
