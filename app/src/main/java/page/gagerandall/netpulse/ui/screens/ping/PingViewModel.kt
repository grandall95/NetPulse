package page.gagerandall.netpulse.ui.screens.ping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.regex.Pattern

class PingViewModel : ViewModel() {

    data class PingResults(
        val minRtt: Float = 0f,
        val avgRtt: Float = 0f,
        val maxRtt: Float = 0f,
        val packetLoss: Float = 0f,
        val status: String = "Idle", // Idle, running, complete, failed
        val isFallback: Boolean = false,
        val packetsSent: Int = 0,
        val packetsReceived: Int = 0,
        val individualRtts: List<Float> = emptyList(),
        val rawLogs: List<String> = emptyList(),
    )

    private val _pingResults = MutableStateFlow(PingResults())
    val pingResults: StateFlow<PingResults> = _pingResults

    private var pingJob: Job? = null
    private var activeProcess: Process? = null

    fun stopPing() {
        pingJob?.cancel()
        pingJob = null
        try {
            activeProcess?.destroy()
        } catch (_: Exception) {
            // ignore
        }
        activeProcess = null
        _pingResults.value = PingResults(status = "Failed", rawLogs = listOf("Ping stopped by user."))
    }

    fun startPing(
        host: String,
        count: Int = 4,
        size: Int = 56,
        timeoutSec: Int = 2,
        ttl: Int = 64
    ) {
        _pingResults.value = PingResults(status = "Running")
        pingJob = viewModelScope.launch(Dispatchers.IO) {
            val cleanedHost = host.trim().lowercase()
            if (cleanedHost.isEmpty()) {
                _pingResults.value = PingResults(status = "Failed", rawLogs = listOf("Error: IP or Hostname is empty."))
                return@launch
            }

            try {
                // Check if binary can be run
                val command = mutableListOf("ping", "-c", count.toString(), "-s", size.toString(), "-W", timeoutSec.toString())
                // Only add -t if running on device and supported (some embedded ping variants do not support -t)
                if (ttl != 64) {
                    command.addAll(listOf("-t", ttl.toString()))
                }
                command.add(cleanedHost)

                val process = Runtime.getRuntime().exec(command.toTypedArray())
                activeProcess = process
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                val logs = mutableListOf<String>()
                val rtts = mutableListOf<Float>()
                var sent = 0
                var received = 0

                val rttPattern = Pattern.compile("time=([0-9.]+)\\s*ms")

                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (!isActive) break
                        val l = line ?: ""
                        logs.add(l)
                        
                        if (l.contains("bytes from")) {
                            sent++
                            received++
                            val matcher = rttPattern.matcher(l)
                            if (matcher.find()) {
                                matcher.group(1)?.toFloatOrNull()?.let {
                                    rtts.add(it)
                                    // Update dynamic progress
                                    if (isActive) {
                                        _pingResults.value = _pingResults.value.copy(
                                            individualRtts = rtts.toList(),
                                            packetsSent = sent,
                                            packetsReceived = received,
                                            rawLogs = logs.toList()
                                        )
                                    }
                                }
                            }
                        } else if (l.contains("Request timeout") || l.contains("timeout")) {
                            sent++
                            if (isActive) {
                                _pingResults.value = _pingResults.value.copy(
                                    packetsSent = sent,
                                    rawLogs = logs.toList()
                                )
                            }
                        }
                    }
                } finally {
                    process.destroy()
                    activeProcess = null
                }

                if (!isActive) return@launch

                // If error or empty, fall back directly
                val errText = errorReader.readText()
                if (logs.isEmpty() && errText.isNotEmpty()) {
                    logs.add("Standard binary failed: $errText")
                    logs.add("Switching to fallback java.net.InetAddress check...")
                    runFallbackInet(cleanedHost, count, logs)
                    return@launch
                }

                // Compile stats from logs or regex
                var min = 0f
                var avg = 0f
                var max = 0f
                var loss = 100f

                if (rtts.isNotEmpty()) {
                    min = rtts.minOrNull() ?: 0f
                    max = rtts.maxOrNull() ?: 0f
                    avg = rtts.average().toFloat()
                    loss = (((count - received).toFloat() / count) * 100).coerceIn(0f, 100f)
                }

                if (isActive) {
                    _pingResults.value = PingResults(
                        minRtt = min,
                        avgRtt = avg,
                        maxRtt = max,
                        packetLoss = loss,
                        status = "Complete",
                        packetsSent = count,
                        packetsReceived = received,
                        individualRtts = rtts,
                        rawLogs = logs
                    )
                }

            } catch (e: Exception) {
                if (isActive) {
                    val logs = mutableListOf("Exception: ${e.message}", "Reverting to InetAddress.isReachable check...")
                    runFallbackInet(cleanedHost, count, logs)
                }
            }
        }
    }

    private suspend fun runFallbackInet(host: String, count: Int, startLogs: MutableList<String>) {
        withContext(Dispatchers.IO) {
            val rtts = mutableListOf<Float>()
            var received = 0

            try {
                val address = InetAddress.getByName(host)
                if (!isActive) return@withContext
                startLogs.add("Resolved Host: ${address.hostAddress}")
                
                for (i in 1..count) {
                    if (!isActive) break
                    val startTime = System.currentTimeMillis()
                    val reachable = address.isReachable(2000)
                    val endTime = System.currentTimeMillis()
                    val duration = (endTime - startTime).toFloat()

                    if (reachable) {
                        received++
                        rtts.add(duration)
                        startLogs.add("Probe $i status: Success. Latency: ${duration}ms")
                    } else {
                        startLogs.add("Probe $i status: Failed (Unreachable/Timeout)")
                    }

                    if (isActive) {
                        _pingResults.value = PingResults(
                            individualRtts = rtts.toList(),
                            packetsSent = i,
                            packetsReceived = received,
                            rawLogs = startLogs.toList(),
                            status = "Running",
                            isFallback = true
                        )
                    }
                    
                    // Sleep with check
                    var slept = 0
                    while (slept < 500 && isActive) {
                        Thread.sleep(50)
                        slept += 50
                    }
                }

                if (isActive) {
                    val min = if (rtts.isEmpty()) 0f else rtts.minOrNull() ?: 0f
                    val max = if (rtts.isEmpty()) 0f else rtts.maxOrNull() ?: 0f
                    val avg = if (rtts.isEmpty()) 0f else rtts.average().toFloat()
                    val loss = ((count - received).toFloat() / count * 100)

                    _pingResults.value = PingResults(
                        minRtt = min,
                        avgRtt = avg,
                        maxRtt = max,
                        packetLoss = loss,
                        status = "Complete",
                        isFallback = true,
                        packetsSent = count,
                        packetsReceived = received,
                        individualRtts = rtts,
                        rawLogs = startLogs
                    )
                }
            } catch (e: Exception) {
                if (isActive) {
                    startLogs.add("Fallback Error: ${e.message}")
                    _pingResults.value = PingResults(
                        status = "Failed",
                        isFallback = true,
                        rawLogs = startLogs
                    )
                }
            }
        }
    }
}
