package page.gagerandall.netpulse.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Speed Test tool.
 * Orchestrates latency checks, multi-connection downloads, and uploads to measure bandwidth.
 */
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
        val errorMessage: String? = null,
    )

    private val _state = MutableStateFlow(SpeedTestState())
    val state: StateFlow<SpeedTestState> = _state

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

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
            val text = response.body.string()

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

    /**
     * Executes parallel download streams to saturate the link and measure bandwidth.
     */
    private suspend fun measureDownload() {
        val parallelConnections = 3
        val downloadSizePerConnection = 25_000_000L // 25MB per connection
        val totalBytesDownloaded = java.util.concurrent.atomic.AtomicLong(0)
        
        val testStartTime = System.nanoTime()
        val durationLimitNs = 5_000_000_000L // 5 seconds
        val isFinished = java.util.concurrent.atomic.AtomicBoolean(false)
        val speeds = mutableListOf<Float>()

        coroutineScope {
            val samplingJob = launch(Dispatchers.Default) {
                var lastBytes = 0L
                var lastTime = System.nanoTime()
                
                while (!isFinished.get()) {
                    delay(200L.milliseconds)
                    val currentBytes = totalBytesDownloaded.get()
                    val currentTime = System.nanoTime()
                    val elapsedSec = (currentTime - lastTime) / 1_000_000_000f
                    if (elapsedSec > 0f) {
                        val deltaBytes = currentBytes - lastBytes
                        val mbps = (deltaBytes * 8f / 1_000_000f) / elapsedSec
                        
                        if (mbps > 0f) {
                            speeds.add(mbps)
                            
                            val totalDurationSec = (currentTime - testStartTime) / 1_000_000_000f
                            val progress = minOf(1.0f, totalDurationSec / 5.0f)
                            
                            _state.value = _state.value.copy(
                                downloadMbps = mbps,
                                downloadProgress = progress,
                                realTimeSpeeds = speeds.toList(),
                                bytesDownloaded = currentBytes
                            )
                        }
                        lastBytes = currentBytes
                        lastTime = currentTime
                    }
                }
            }

            val downloadJobs = List(parallelConnections) {
                launch(Dispatchers.IO) {
                    try {
                        val request = Request.Builder()
                            .url("https://speed.cloudflare.com/__down?bytes=$downloadSizePerConnection")
                            .build()

                        okHttpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw IOException("Download block failed: $response")
                            val responseBody = response.body

                            val inputStream = responseBody.byteStream()
                            val buffer = ByteArray(16384)
                            var bytesRead: Int

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                val elapsedNano = System.nanoTime() - testStartTime
                                if (elapsedNano >= durationLimitNs) {
                                    break
                                }
                                totalBytesDownloaded.addAndGet(bytesRead.toLong())
                            }
                        }
                    } catch (e: Exception) {
                        // ignore or log
                    }
                }
            }

            downloadJobs.joinAll()
            isFinished.set(true)
            samplingJob.join()
        }

        val totalDurationSec = (System.nanoTime() - testStartTime) / 1_000_000_000f
        val avgDownload = if (speeds.size > 5) {
            speeds.subList(5, speeds.size).average().toFloat()
        } else if (speeds.isNotEmpty()) {
            speeds.average().toFloat()
        } else {
            0f
        }

        _state.value = _state.value.copy(
            downloadMbps = avgDownload,
            downloadProgress = 1.0f,
            durationSec = totalDurationSec
        )
    }

    /**
     * Executes parallel upload streams using custom CountingRequestBody to track progress.
     */
    private suspend fun measureUpload() {
        val parallelConnections = 3
        val uploadSizePerConnection = 15_000_000L // 15MB per connection
        val totalBytesUploaded = java.util.concurrent.atomic.AtomicLong(0)
        
        val testStartTime = System.nanoTime()
        val durationLimitNs = 5_000_000_000L // 5 seconds
        val stopUpload = java.util.concurrent.atomic.AtomicBoolean(false)
        val speeds = mutableListOf<Float>()

        coroutineScope {
            val samplingJob = launch(Dispatchers.Default) {
                var lastBytes = 0L
                var lastTime = System.nanoTime()
                
                while (!stopUpload.get()) {
                    delay(200L.milliseconds)
                    val currentBytes = totalBytesUploaded.get()
                    val currentTime = System.nanoTime()
                    val elapsedSec = (currentTime - lastTime) / 1_000_000_000f
                    if (elapsedSec > 0f) {
                        val deltaBytes = currentBytes - lastBytes
                        val mbps = (deltaBytes * 8f / 1_000_000f) / elapsedSec
                        
                        if (mbps > 0f) {
                            speeds.add(mbps)
                            
                            val totalDurationSec = (currentTime - testStartTime) / 1_000_000_000f
                            val progress = minOf(1.0f, totalDurationSec / 5.0f)
                            
                            _state.value = _state.value.copy(
                                uploadMbps = mbps,
                                uploadProgress = progress,
                                realTimeSpeeds = _state.value.realTimeSpeeds + mbps,
                                bytesUploaded = currentBytes
                            )
                        }
                        lastBytes = currentBytes
                        lastTime = currentTime
                    }
                }
            }

            val uploadJobs = List(parallelConnections) {
                launch(Dispatchers.IO) {
                    try {
                        val requestBody = CountingRequestBody(
                            contentType = "application/octet-stream".toMediaType(),
                            size = uploadSizePerConnection,
                            onBytesWritten = { bytes ->
                                totalBytesUploaded.addAndGet(bytes)
                            },
                            shouldStop = {
                                val elapsed = System.nanoTime() - testStartTime
                                val stop = elapsed >= durationLimitNs || stopUpload.get()
                                if (stop) {
                                    stopUpload.set(true)
                                }
                                stop
                            },
                        )

                        val request = Request.Builder()
                            .url("https://speed.cloudflare.com/__up")
                            .post(requestBody)
                            .build()

                        okHttpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw IOException("Upload block failed: $response")
                        }
                    } catch (e: Exception) {
                        // ignore or log
                    }
                }
            }

            uploadJobs.joinAll()
            stopUpload.set(true)
            samplingJob.join()
        }

        val totalDurationSec = (System.nanoTime() - testStartTime) / 1_000_000_000f
        val avgUpload = if (speeds.size > 5) {
            speeds.subList(5, speeds.size).average().toFloat()
        } else if (speeds.isNotEmpty()) {
            speeds.average().toFloat()
        } else {
            0f
        }

        val current = _state.value
        _state.value = current.copy(
            uploadMbps = avgUpload,
            uploadProgress = 1.0f,
            durationSec = current.durationSec + totalDurationSec
        )
    }
}

private class CountingRequestBody(
    private val contentType: MediaType?,
    private val size: Long,
    private val onBytesWritten: (Long) -> Unit,
    private val shouldStop: () -> Boolean
) : RequestBody() {
    override fun contentType() = contentType
    override fun contentLength() = size

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(16384)
        var written = 0L
        while (written < size) {
            if (shouldStop()) break
            val toWrite = minOf(buffer.size.toLong(), size - written).toInt()
            sink.write(buffer, 0, toWrite)
            sink.flush()
            written += toWrite
            onBytesWritten(toWrite.toLong())
        }
    }
}
