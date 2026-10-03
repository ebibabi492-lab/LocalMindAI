package com.example.downloader

import android.content.Context
import android.net.Uri
import com.example.model.ModelCopyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 100% Offline Model File Manager.
 * No internet connection, no downloading links or remote HTTP calls.
 * Manages local directory storage and imports model files from device storage.
 */
class ModelDownloader(private val context: Context) {

    private val _copyState = MutableStateFlow<ModelCopyState>(ModelCopyState.Idle)
    val copyState: StateFlow<ModelCopyState> = _copyState.asStateFlow()

    fun getModelsDirectory(): File {
        val dir = context.getExternalFilesDir("models") ?: File(context.filesDir, "models")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getModelsDirectoryPath(): String {
        return getModelsDirectory().absolutePath
    }

    fun getModelFile(fileName: String): File {
        return File(getModelsDirectory(), fileName)
    }

    fun isModelFilePresent(fileName: String, expectedMinBytes: Long = 1024 * 1024): Boolean {
        val file = getModelFile(fileName)
        return file.exists() && file.length() >= expectedMinBytes
    }

    fun deleteModel(fileName: String): Boolean {
        val file = getModelFile(fileName)
        val deleted = file.delete()
        _copyState.value = ModelCopyState.Idle
        return deleted
    }

    /**
     * Copies a local .litertlm model file from device storage into the app's models directory offline.
     */
    suspend fun copyModelFromUri(uri: Uri, targetFileName: String): Result<File> = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val safeName = targetFileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val destFile = getModelFile(safeName)
        val tempFile = File(getModelsDirectory(), "$safeName.tmp")

        try {
            var totalBytes = -1L
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                totalBytes = pfd.statSize
            }

            _copyState.value = ModelCopyState.Copying(0f, 0L, totalBytes, safeName)

            val inputStream: InputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open file from storage"))

            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(64 * 1024)
            var bytesCopied = 0L
            var read: Int
            var lastUpdate = System.currentTimeMillis()

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate >= 200 || (totalBytes > 0 && bytesCopied == totalBytes)) {
                            val progress = if (totalBytes > 0) {
                                (bytesCopied.toFloat() / totalBytes).coerceIn(0f, 1f)
                            } else 0f
                            _copyState.value = ModelCopyState.Copying(progress, bytesCopied, totalBytes, safeName)
                            lastUpdate = now
                        }
                    }
                    output.flush()
                }
            }

            if (destFile.exists()) {
                destFile.delete()
            }
            if (tempFile.renameTo(destFile)) {
                _copyState.value = ModelCopyState.Success(safeName)
                Result.success(destFile)
            } else {
                _copyState.value = ModelCopyState.Error("Failed to rename copied model file")
                Result.failure(Exception("Failed to finalize copied file"))
            }
        } catch (e: Exception) {
            tempFile.delete()
            _copyState.value = ModelCopyState.Error(e.localizedMessage ?: "File copy failed")
            Result.failure(e)
        }
    }

    fun resetCopyState() {
        _copyState.value = ModelCopyState.Idle
    }
}
