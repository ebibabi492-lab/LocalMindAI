package com.example.downloader

import android.content.Context
import com.example.model.DownloadState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ModelDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getModelsDirectory(): File {
        val dir = context.getExternalFilesDir("models") ?: File(context.filesDir, "models")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
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
        val tmpFile = File(getModelsDirectory(), "$fileName.tmp")
        tmpFile.delete()
        val deleted = file.delete()
        updateState(fileName, DownloadState.Idle)
        return deleted
    }

    fun startDownload(modelId: String, fileName: String, url: String) {
        if (activeJobs[modelId]?.isActive == true) return

        val job = scope.launch {
            val destinationFile = getModelFile(fileName)
            val tempFile = File(getModelsDirectory(), "$fileName.tmp")

            try {
                updateState(modelId, DownloadState.Downloading(0f, 0L, 0L, "0 KB/s"))

                var downloadedBytes = 0L
                val requestBuilder = Request.Builder().url(url)
                if (tempFile.exists() && tempFile.length() > 0) {
                    downloadedBytes = tempFile.length()
                    requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
                }

                val response = client.newCall(requestBuilder.build()).execute()
                if (!response.isSuccessful && response.code != 206) {
                    // If range request fails, restart from 0
                    tempFile.delete()
                    downloadedBytes = 0L
                    val retryResponse = client.newCall(Request.Builder().url(url).build()).execute()
                    if (!retryResponse.isSuccessful) {
                        updateState(modelId, DownloadState.Failed("HTTP error ${retryResponse.code}"))
                        return@launch
                    }
                    writeResponseBody(retryResponse.body?.byteStream(), retryResponse.body?.contentLength() ?: -1L, tempFile, downloadedBytes, modelId, destinationFile)
                } else {
                    val totalLength = (response.body?.contentLength() ?: -1L) + downloadedBytes
                    writeResponseBody(response.body?.byteStream(), totalLength, tempFile, downloadedBytes, modelId, destinationFile)
                }

            } catch (e: CancellationException) {
                updateState(modelId, DownloadState.Paused)
            } catch (e: Exception) {
                updateState(modelId, DownloadState.Failed(e.localizedMessage ?: "Download failed"))
            } finally {
                activeJobs.remove(modelId)
            }
        }
        activeJobs[modelId] = job
    }

    private suspend fun writeResponseBody(
        inputStream: InputStream?,
        totalBytes: Long,
        tempFile: File,
        initialDownloaded: Long,
        modelId: String,
        destinationFile: File
    ) = withContext(Dispatchers.IO) {
        if (inputStream == null) {
            updateState(modelId, DownloadState.Failed("Empty response body"))
            return@withContext
        }

        var downloadedBytes = initialDownloaded
        val append = initialDownloaded > 0
        val outputStream = FileOutputStream(tempFile, append)

        val buffer = ByteArray(64 * 1024)
        var read: Int
        var lastTime = System.currentTimeMillis()
        var bytesSinceLastSample = 0L

        try {
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                downloadedBytes += read
                bytesSinceLastSample += read

                val now = System.currentTimeMillis()
                val elapsed = now - lastTime
                if (elapsed >= 350) {
                    val speedBytesPerSec = if (elapsed > 0) (bytesSinceLastSample * 1000) / elapsed else 0L
                    val speedText = formatSpeed(speedBytesPerSec)
                    val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                    updateState(
                        modelId,
                        DownloadState.Downloading(
                            progress = progress.coerceIn(0f, 1f),
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes,
                            speedText = speedText
                        )
                    )
                    lastTime = now
                    bytesSinceLastSample = 0L
                }
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Rename temp to destination
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            val renamed = tempFile.renameTo(destinationFile)
            if (renamed) {
                updateState(modelId, DownloadState.Completed)
            } else {
                updateState(modelId, DownloadState.Failed("Failed to save model file"))
            }

        } catch (e: Exception) {
            outputStream.close()
            inputStream.close()
            throw e
        }
    }

    fun pauseDownload(modelId: String) {
        activeJobs[modelId]?.cancel()
        activeJobs.remove(modelId)
        updateState(modelId, DownloadState.Paused)
    }

    fun cancelDownload(modelId: String, fileName: String) {
        activeJobs[modelId]?.cancel()
        activeJobs.remove(modelId)
        val tempFile = File(getModelsDirectory(), "$fileName.tmp")
        tempFile.delete()
        updateState(modelId, DownloadState.Idle)
    }

    private fun updateState(modelId: String, state: DownloadState) {
        _downloadStates.value = _downloadStates.value.toMutableMap().apply {
            put(modelId, state)
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec.toFloat() / (1024 * 1024))
            bytesPerSec >= 1024 -> String.format("%d KB/s", bytesPerSec / 1024)
            else -> "$bytesPerSec B/s"
        }
    }
}
