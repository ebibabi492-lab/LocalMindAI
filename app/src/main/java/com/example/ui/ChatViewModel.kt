package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.ModelRepository
import com.example.data.repository.SettingsRepository
import com.example.downloader.ModelDownloader
import com.example.inference.LocalLLMEngine
import com.example.model.AppSettings
import com.example.model.DownloadState
import com.example.model.ModelInferenceStatus
import com.example.model.ModelInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val chatRepository = ChatRepository(db.chatDao())
    val settingsRepository = SettingsRepository(application)
    val modelDownloader = ModelDownloader(application)
    val modelRepository = ModelRepository(application, modelDownloader)
    val llmEngine = LocalLLMEngine(application)

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    val conversationsList: StateFlow<List<ConversationEntity>> =
        chatRepository.getConversations().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    val modelsList: StateFlow<List<ModelInfo>> = modelRepository.models
    val downloadStates: StateFlow<Map<String, DownloadState>> = modelDownloader.downloadStates

    val inferenceStatus: StateFlow<ModelInferenceStatus> = llmEngine.status
    val inferenceStatusMessage: StateFlow<String> = llmEngine.statusMessage
    val activeBackend: StateFlow<String> = llmEngine.activeBackend

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()

    private var activeGenerationJob: Job? = null
    private var messagesCollectJob: Job? = null

    init {
        // Observe model download completions to refresh model list & auto-select if needed
        viewModelScope.launch {
            modelDownloader.downloadStates.collect { states ->
                val hasCompleted = states.values.any { it is DownloadState.Completed }
                if (hasCompleted) {
                    modelRepository.refreshModelsList()
                }
            }
        }

        // Initialize model on startup if previously active
        viewModelScope.launch {
            val savedModelId = settings.value.activeModelId
            modelRepository.refreshModelsList()
            val availableModels = modelRepository.models.value
            val targetModel = availableModels.find { it.id == savedModelId && it.isInstalled }
                ?: availableModels.find { it.isInstalled }

            if (targetModel != null && targetModel.localPath != null) {
                selectActiveModel(targetModel.id)
            }
        }
    }

    fun selectConversation(conversationId: Long) {
        viewModelScope.launch {
            val conv = chatRepository.getConversationById(conversationId)
            _currentConversation.value = conv
            observeMessages(conversationId)
        }
    }

    private fun observeMessages(conversationId: Long) {
        messagesCollectJob?.cancel()
        messagesCollectJob = viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun startNewConversation() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
        _currentStreamingText.value = ""
        _currentConversation.value = null
        _messages.value = emptyList()
        messagesCollectJob?.cancel()
        llmEngine.startNewConversation(settings.value.temperature)
    }

    fun sendMessage(rawPrompt: String) {
        val prompt = rawPrompt.trim()
        if (prompt.isEmpty() || _isGenerating.value) return

        activeGenerationJob = viewModelScope.launch {
            try {
                var conv = _currentConversation.value
                if (conv == null) {
                    val title = if (prompt.length > 32) prompt.take(32) + "..." else prompt
                    val activeModel = settings.value.activeModelId ?: "localmind"
                    val convId = chatRepository.createConversation(title, activeModel)
                    conv = chatRepository.getConversationById(convId)
                    _currentConversation.value = conv
                    if (conv != null) {
                        observeMessages(conv.id)
                    }
                }

                val currentConvId = conv?.id ?: return@launch
                chatRepository.addMessage(currentConvId, "user", prompt)

                _isGenerating.value = true
                _currentStreamingText.value = ""

                var accumulatedResponse = ""
                llmEngine.generateStreamingResponse(prompt).collect { chunk ->
                    accumulatedResponse = chunk
                    _currentStreamingText.value = chunk
                }

                // Finished generation, save to DB
                if (accumulatedResponse.isNotBlank()) {
                    chatRepository.addMessage(currentConvId, "assistant", accumulatedResponse.trim())
                }

            } catch (e: Exception) {
                val currentConvId = _currentConversation.value?.id
                if (currentConvId != null && _currentStreamingText.value.isNotBlank()) {
                    chatRepository.addMessage(
                        currentConvId,
                        "assistant",
                        _currentStreamingText.value.trim()
                    )
                }
            } finally {
                _isGenerating.value = false
                _currentStreamingText.value = ""
                activeGenerationJob = null
            }
        }
    }

    fun stopGeneration() {
        val currentText = _currentStreamingText.value
        val convId = _currentConversation.value?.id
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _isGenerating.value = false

        if (convId != null && currentText.isNotBlank()) {
            viewModelScope.launch {
                chatRepository.addMessage(convId, "assistant", currentText.trim())
                _currentStreamingText.value = ""
            }
        } else {
            _currentStreamingText.value = ""
        }
    }

    fun selectActiveModel(modelId: String) {
        viewModelScope.launch {
            val model = modelRepository.getModelById(modelId) ?: return@launch
            if (!model.isInstalled || model.localPath == null) return@launch

            settingsRepository.updateActiveModelId(modelId)

            llmEngine.loadModel(
                modelFilePath = model.localPath,
                preferredBackend = settings.value.preferredBackend,
                temperature = settings.value.temperature,
                maxTokens = settings.value.maxResponseTokens
            )
        }
    }

    fun downloadModel(model: ModelInfo) {
        modelDownloader.startDownload(model.id, model.fileName, model.downloadUrl)
    }

    fun pauseDownload(modelId: String) {
        modelDownloader.pauseDownload(modelId)
    }

    fun cancelDownload(model: ModelInfo) {
        modelDownloader.cancelDownload(model.id, model.fileName)
    }

    fun deleteModel(model: ModelInfo) {
        viewModelScope.launch {
            if (settings.value.activeModelId == model.id) {
                llmEngine.unloadModel()
                settingsRepository.updateActiveModelId(null)
            }
            modelRepository.deleteModel(model)
        }
    }

    fun importLocalModel(uri: Uri, fileName: String?) {
        viewModelScope.launch {
            val result = modelRepository.importModelFromUri(uri, fileName)
            result.onSuccess { imported ->
                selectActiveModel(imported.id)
            }
        }
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            chatRepository.clearAllConversations()
            startNewConversation()
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            chatRepository.deleteConversation(id)
            if (_currentConversation.value?.id == id) {
                startNewConversation()
            }
        }
    }

    fun updateLanguage(lang: String) {
        settingsRepository.updateLanguage(lang)
    }

    fun updateTheme(theme: String) {
        settingsRepository.updateTheme(theme)
    }

    fun updateTemperature(temp: Float) {
        settingsRepository.updateTemperature(temp)
    }

    fun updateMaxTokens(tokens: Int) {
        settingsRepository.updateMaxTokens(tokens)
    }

    fun updatePreferredBackend(backend: String) {
        settingsRepository.updatePreferredBackend(backend)
        // If model already loaded, reload with new backend preference
        val activeId = settings.value.activeModelId
        if (activeId != null) {
            selectActiveModel(activeId)
        }
    }

    fun getAvailableRamMb(): Long = llmEngine.getAvailableRamMb()
}
