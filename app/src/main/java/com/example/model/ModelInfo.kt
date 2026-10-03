package com.example.model

data class ModelInfo(
    val id: String,
    val name: String,
    val architecture: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val supportedLanguages: String, // e.g. "English, Persian, Multilingual"
    val supportedLanguagesFa: String, // e.g. "فارسی، انگلیسی، چندزبانه"
    val minRamGb: Int,
    val downloadUrl: String,
    val fileName: String,
    val description: String,
    val descriptionFa: String,
    val isInstalled: Boolean = false,
    val isCustom: Boolean = false,
    val localPath: String? = null
)

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedText: String
    ) : DownloadState()
    data object Paused : DownloadState()
    data class Failed(val error: String) : DownloadState()
    data object Completed : DownloadState()
}

data class AppSettings(
    val language: String = "system", // "system", "fa", "en"
    val theme: String = "system", // "system", "light", "dark"
    val maxResponseTokens: Int = 1024,
    val temperature: Float = 0.7f,
    val activeModelId: String? = null,
    val preferredBackend: String = "auto" // "auto", "gpu", "cpu"
)

enum class ModelInferenceStatus {
    NO_MODEL_SELECTED,
    LOADING,
    READY_OFFLINE,
    ERROR
}
