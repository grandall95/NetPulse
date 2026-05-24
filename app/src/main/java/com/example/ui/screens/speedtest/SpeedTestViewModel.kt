package com.example.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class SpeedTestViewModel : ViewModel() {

    data class SpeedTestState(
        val status: String = "Idle", // Idle, latency, download, upload, complete, failed
        val serverLocation: String = "Detecting...",
        val ipAddress: String = "Unknown",
        val latencyMs: Float = 0f,
        val downloadMbps: Float = 0f,
        val uploadMbps: Float = 0f,
        val downloadProgress: Float = 0f, // 0.0 to 1.0
        val uploadProgress: Float = 0f, // 0.0 to 1.0
        val realTimeSpeeds: List<Float> = emptyList(), // real-time measurements in Mbps
        val bytesDownloaded: Long = 0,
        val bytesUploaded: Long = 0,
        val durationSec: Float = 0f,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(SpeedTestState())
    val state: StateFlow<SpeedTestState> = _state

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun clear() {
        _state.value = SpeedTestState()
    }

    fun runSpeedTest() {
        _state.value = SpeedTestState(status = "Measuring Latency...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Latency & trace metadata check
                val latencyAndLoc = fetchLatencyAndLocation()
                _state.value = _state.value.copy(
                    status = "Downloading...",
                    latencyMs = latencyAndLoc.latency,
                    serverLocation = latencyAndLoc.location,
                    ipAddress = latencyAndLoc.ip
                )

                // 2. Download Speed Test (Download blocks of 1MB, 2MB, 5MB sequentially to show progression)
                measureDownload()

                // Update state
                _state.value = _state.value.copy(status = "Uploading...")

                // 3. Upload Speed Test (POST blocks of bytes)
                measureUpload()

                // Complete
                val current = _state.value
                _state.value = current.copy(
                    status = "Complete",
                    downloadProgress = 1.0f,
                    uploadProgress = 1.0f
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = "Failed",
                    errorMessage = e.message ?: "An unexpected connection error occurred."
                )
            }
        }
    }

    private data class TraceData(val latency: Float, val location: String, val ip: String)

    private fun fetchLatencyAndLocation(): TraceData {
        val startTime = System.currentTimeMillis()
        val request = Request.Builder()
            .url("https://speed.cloudflare.com/cdn-cgi/trace")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed check: $response")
            val duration = (System.currentTimeMillis() - startTime).toFloat()
            val text = response.body?.string() ?: ""

            var location = "US"
            var ipAddress = "0.0.0.0"

            text.split("\n").forEach { line ->
                val parts = line.split("=")
                if (parts.size == 2) {
                    when (parts[0]) {
                        "colo" -> location = parts[1]
                        "ip" -> ipAddress = parts[1]
                    }
                }
            }
            return TraceData(duration, location, ipAddress)
        }
    }

    private fun measureDownload() {
        // Download 2 blocks: 1MB and then 3MB
        val bytesList = listOf(1_000_000L, 3_000_000L)
        val speeds = mutableListOf<Float>()
        var totalBytes = 0L
        val testStartTime = System.currentTimeMillis()

        bytesList.forEachIndexed { index, bytesCount ->
            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__down?bytes=$bytesCount")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Download block failed: $response")
                val responseBody = response.body ?: throw IOException("Empty download payload")

                val inputStream = responseBody.byteStream()
                val buffer = ByteArray(16384)
                var bytesRead: Int
                val blockStartTime = System.nanoTime()
                var bytesReadInBlock = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    bytesReadInBlock += bytesRead
                    totalBytes += bytesRead

                    val elapsedNano = System.nanoTime() - blockStartTime
                    if (elapsedNano > 0) {
                        // Calculate instant mbps
                        val mbps = (bytesReadInBlock * 8f / 1_000_000f) / (elapsedNano / 1_000_000_000f)
                        if (mbps > 0f && mbps < 10000f) {
                            speeds.add(mbps)
                            _state.value = _state.value.copy(
                                downloadMbps = mbps,
                                downloadProgress = (index * 0.5f) + ((bytesReadInBlock.toFloat() / bytesCount) * 0.5f),
                                realTimeSpeeds = speeds.toList(),
                                bytesDownloaded = totalBytes
                            )
                        }
                    }
                }
            }
        }

        val totalDurationSec = (System.currentTimeMillis() - testStartTime) / 1000f
        val avgDownload = if (speeds.isNotEmpty()) speeds.average().toFloat() else 0f

        _state.value = _state.value.copy(
            downloadMbps = avgDownload,
            downloadProgress = 1.0f,
            durationSec = totalDurationSec
        )
    }

    private fun measureUpload() {
        // Post 2 blocks of 1MB each
        val uploadBytesCount = 1_000_000
        val uploadData = ByteArray(uploadBytesCount) // Filled with dummy null bytes
        val speeds = mutableListOf<Float>()
        var totalBytesUploaded = 0L
        val testStartTime = System.currentTimeMillis()

        for (i in 1..2) {
            val requestBody = uploadData.toRequestBody("application/octet-stream".toMediaType())
            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__up")
                .post(requestBody)
                .build()

            val blockStartTime = System.nanoTime()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Upload failed block $i: $response")
                val elapsedNano = System.nanoTime() - blockStartTime
                totalBytesUploaded += uploadBytesCount

                if (elapsedNano > 0) {
                    val mbps = (uploadBytesCount * 8f / 1_000_000f) / (elapsedNano / 1_000_000_000f)
                    speeds.add(mbps)
                    _state.value = _state.value.copy(
                        uploadMbps = mbps,
                        uploadProgress = i * 0.5f,
                        realTimeSpeeds = _state.value.realTimeSpeeds + mbps,
                        bytesUploaded = totalBytesUploaded
                    )
                }
            }
        }

        val avgUpload = if (speeds.isNotEmpty()) speeds.average().toFloat() else 0f
        val current = _state.value
        _state.value = current.copy(
            uploadMbps = avgUpload,
            uploadProgress = 1.0f,
            durationSec = current.durationSec + ((System.currentTimeMillis() - testStartTime) / 1000f)
        )
    }
}
