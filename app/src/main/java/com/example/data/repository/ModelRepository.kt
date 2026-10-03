package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.downloader.ModelDownloader
import com.example.model.ModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ModelRepository(
    private val context: Context,
    val downloader: ModelDownloader
) {

    private val predefinedModels = listOf(
        ModelInfo(
            id = "qwen2.5-0.5b",
            name = "Qwen 2.5 0.5B Instruct",
            architecture = "Qwen2.5 LiteRT-LM",
            sizeBytes = 468_000_000L,
            formattedSize = "446 MB",
            supportedLanguages = "Persian, English, Multilingual",
            supportedLanguagesFa = "فارسی، انگلیسی، چندزبانه",
            minRamGb = 2,
            downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct-litert/resolve/main/model.litertlm",
            fileName = "qwen2.5-0.5b-instruct.litertlm",
            description = "Ultra-fast lightweight model. Highly recommended for phones with 2GB-3GB RAM. Fast inference speed and low battery impact.",
            descriptionFa = "مدل بسیار سبک و پرسرعت. مناسب برای گوشی‌های با ۲ تا ۳ گیگابایت رم. پاسخگویی سریع و مصرف باتری بسیار کم."
        ),
        ModelInfo(
            id = "gemma-3-1b",
            name = "Gemma 3 1B IT",
            architecture = "Gemma 3 LiteRT-LM",
            sizeBytes = 1_280_000_000L,
            formattedSize = "1.2 GB",
            supportedLanguages = "Persian, English, Multilingual",
            supportedLanguagesFa = "فارسی، انگلیسی، چندزبانه",
            minRamGb = 3,
            downloadUrl = "https://huggingface.co/litert-community/gemma-3-1b-it-litert/resolve/main/model.litertlm",
            fileName = "gemma-3-1b-it.litertlm",
            description = "Google Gemma 3 optimized for mobile edge hardware. Great reasoning, knowledge, and multilingual accuracy.",
            descriptionFa = "مدل جمای ۳ گوگل بهینه‌شده برای موبایل. قدرت استدلال، دانش عمومی و ترجمه چندزبانه عالی."
        ),
        ModelInfo(
            id = "qwen2.5-1.5b",
            name = "Qwen 2.5 1.5B Instruct",
            architecture = "Qwen2.5 LiteRT-LM",
            sizeBytes = 1_470_000_000L,
            formattedSize = "1.4 GB",
            supportedLanguages = "Persian, English, Multilingual",
            supportedLanguagesFa = "فارسی، انگلیسی، چندزبانه",
            minRamGb = 4,
            downloadUrl = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct-litert/resolve/main/model.litertlm",
            fileName = "qwen2.5-1.5b-instruct.litertlm",
            description = "Comprehensive language reasoning and higher precision. Recommended for devices with 4GB+ RAM.",
            descriptionFa = "کیفیت پاسخگویی بالا و استدلال دقیق‌تر. مناسب دستگاه‌های با حداقل ۴ گیگابایت حافظه رم."
        ),
        ModelInfo(
            id = "smollm2-360m",
            name = "SmolLM2 360M Instruct",
            architecture = "SmolLM2 LiteRT-LM",
            sizeBytes = 345_000_000L,
            formattedSize = "329 MB",
            supportedLanguages = "English, Multilingual",
            supportedLanguagesFa = "انگلیسی، چندزبانه",
            minRamGb = 2,
            downloadUrl = "https://huggingface.co/litert-community/SmolLM2-360M-Instruct-litert/resolve/main/model.litertlm",
            fileName = "smollm2-360m-instruct.litertlm",
            description = "Extremely small footprint. Instant local startup and tiny memory usage for quick tasks.",
            descriptionFa = "حجم بسیار اندک و حداقل مصرف رم برای اجرای سریع حتی روی گوشی‌های اقتصادی."
        )
    )

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    init {
        refreshModelsList()
    }

    fun refreshModelsList() {
        val updated = predefinedModels.map { model ->
            val isPresent = downloader.isModelFilePresent(model.fileName)
            val file = if (isPresent) downloader.getModelFile(model.fileName) else null
            model.copy(
                isInstalled = isPresent,
                localPath = file?.absolutePath
            )
        }
        // Also scan for any custom user imported models in directory
        val modelsDir = downloader.getModelsDirectory()
        val customFiles = modelsDir.listFiles { f ->
            f.isFile && (f.name.endsWith(".litertlm") || f.name.endsWith(".bin")) &&
                predefinedModels.none { it.fileName == f.name }
        }?.map { file ->
            ModelInfo(
                id = "custom-${file.name}",
                name = file.nameWithoutExtension.replace('-', ' ').replace('_', ' ').capitalizeWords(),
                architecture = "Custom LiteRT-LM",
                sizeBytes = file.length(),
                formattedSize = formatBytes(file.length()),
                supportedLanguages = "Custom",
                supportedLanguagesFa = "سفارشی",
                minRamGb = 2,
                downloadUrl = "",
                fileName = file.name,
                description = "Locally imported model file: ${file.name}",
                descriptionFa = "مدل محلی اضافه شده: ${file.name}",
                isInstalled = true,
                isCustom = true,
                localPath = file.absolutePath
            )
        } ?: emptyList()

        _models.value = updated + customFiles
    }

    suspend fun importModelFromUri(uri: Uri, originalFileName: String?): Result<ModelInfo> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val safeName = (originalFileName ?: "imported_model.litertlm")
                .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(downloader.getModelsDirectory(), safeName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Unable to read selected file"))

            refreshModelsList()
            val imported = _models.value.find { it.fileName == safeName }
                ?: return@withContext Result.failure(Exception("Failed to register imported model"))

            Result.success(imported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getModelById(id: String): ModelInfo? {
        return _models.value.find { it.id == id }
    }

    fun deleteModel(modelInfo: ModelInfo): Boolean {
        val success = downloader.deleteModel(modelInfo.fileName)
        refreshModelsList()
        return success
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format("%d KB", bytes / 1024)
            else -> "$bytes B"
        }
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
