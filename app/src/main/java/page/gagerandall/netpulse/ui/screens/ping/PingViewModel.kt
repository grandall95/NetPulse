package page.gagerandall.netpulse.ui.screens.ping

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.system.StructTimeval
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
import java.io.FileDescriptor
import java.io.InputStreamReader
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.regex.Pattern

/**
 * ViewModel for the Ping Analyzer tool.
 * Implements a tiered ping strategy to maximize compatibility across different Android devices.
 */
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

    /**
     * Starts the ping process using a tiered approach:
     * 1. Tier 1: Unprivileged ICMP datagram sockets (Modern Android approach).
     * 2. Tier 2: Native 'ping' binary execution (Legacy/Fallback).
     * 3. Tier 3: Java InetAddress.isReachable (Safe fallback).
     */
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

            // Tier 1: Try unprivileged ICMP datagram sockets
            val socketSuccess = tryIcmpSocketPing(cleanedHost, count, size, timeoutSec, ttl)
            if (socketSuccess) return@launch

            // Tier 2: Try native command execution (fallback)
            val binarySuccess = tryNativeBinaryPing(cleanedHost, count, size, timeoutSec, ttl)
            if (binarySuccess) return@launch

            // Tier 3: Failsafe Java InetAddress / HTTP
            if (isActive) {
                val fallbackLogs = mutableListOf(
                    "Unprivileged ICMP socket check failed or not permitted by kernel.",
                    "Native Ping binary execution failed/restricted on this device.",
                    "Initiating HTTP/Java Reachability fallback..."
                )
                runFallbackInet(cleanedHost, count, fallbackLogs)
            }
        }
    }

    /**
     * Tier 1: Uses AF_INET, SOCK_DGRAM, IPPROTO_ICMP to send ICMP packets without root.
     */
    private suspend fun tryIcmpSocketPing(
        host: String,
        count: Int,
        size: Int,
        timeoutSec: Int,
        ttl: Int
    ): Boolean {
        return withContext(Dispatchers.IO) {
            var socketFd: FileDescriptor? = null
            try {
                socketFd = Os.socket(
                    OsConstants.AF_INET,
                    OsConstants.SOCK_DGRAM,
                    OsConstants.IPPROTO_ICMP
                )

                val timeoutValue = StructTimeval.fromMillis((timeoutSec * 1000).toLong())
                Os.setsockoptTimeval(socketFd, OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO, timeoutValue)
                Os.setsockoptInt(socketFd, OsConstants.IPPROTO_IP, OsConstants.IP_TTL, ttl)

                val address = InetAddress.getByName(host)
                val logs = mutableListOf<String>()
                val rtts = mutableListOf<Float>()
                var sent = 0
                var received = 0

                logs.add("Pinging ${address.hostAddress} using unprivileged ICMP socket...")

                for (i in 1..count) {
                    if (!isActive) break

                    val requestPacket = constructIcmpEchoRequest(i, size)
                    val startTime = System.currentTimeMillis()

                    try {
                        Os.sendto(socketFd, ByteBuffer.wrap(requestPacket), 0, address, 0)
                        sent++

                        val responseBuffer = ByteBuffer.allocate(65535)
                        Os.recvfrom(socketFd, responseBuffer, 0, null)

                        val duration = (System.currentTimeMillis() - startTime).toFloat()
                        
                        responseBuffer.flip()
                        val bytesReceived = responseBuffer.remaining()
                        if (bytesReceived >= 8) {
                            val replyType = responseBuffer.get(0).toInt() and 0xFF
                            val replyCode = responseBuffer.get(1).toInt() and 0xFF
                            val replySequence = responseBuffer.getShort(6)

                            if (replyType == 0 && replyCode == 0) {
                                received++
                                rtts.add(duration)
                                logs.add("$bytesReceived bytes from ${address.hostAddress}: icmp_seq=$replySequence time=${String.format("%.1f", duration)} ms")
                            } else {
                                logs.add("Received non-reply ICMP type=$replyType code=$replyCode from ${address.hostAddress}")
                            }
                        } else {
                            logs.add("Received truncated packet of size $bytesReceived bytes")
                        }
                    } catch (e: ErrnoException) {
                        if (e.errno == OsConstants.EAGAIN) {
                            logs.add("Request timeout for icmp_seq=$i")
                        } else {
                            logs.add("Socket error on seq=$i: ${e.message}")
                        }
                    } catch (e: Exception) {
                        logs.add("Error sending/receiving seq=$i: ${e.message}")
                    }

                    if (isActive) {
                        _pingResults.value = _pingResults.value.copy(
                            individualRtts = rtts.toList(),
                            packetsSent = sent,
                            packetsReceived = received,
                            rawLogs = logs.toList()
                        )
                    }

                    if (i < count && isActive) {
                        var slept = 0
                        while (slept < 1000 && isActive) {
                            Thread.sleep(50)
                            slept += 50
                        }
                    }
                }

                if (!isActive) return@withContext false

                if (sent > 0) {
                    val min = rtts.minOrNull() ?: 0f
                    val max = rtts.maxOrNull() ?: 0f
                    val avg = rtts.average().toFloat()
                    val loss = (((sent - received).toFloat() / sent) * 100).coerceIn(0f, 100f)

                    _pingResults.value = PingResults(
                        minRtt = min,
                        avgRtt = avg,
                        maxRtt = max,
                        packetLoss = loss,
                        status = "Complete",
                        isFallback = false,
                        packetsSent = sent,
                        packetsReceived = received,
                        individualRtts = rtts,
                        rawLogs = logs
                    )
                    return@withContext true
                }
                return@withContext false
            } catch (e: Exception) {
                return@withContext false
            } finally {
                socketFd?.let {
                    try { Os.close(it) } catch (ignored: Exception) {}
                }
            }
        }
    }

    private suspend fun tryNativeBinaryPing(
        host: String,
        count: Int,
        size: Int,
        timeoutSec: Int,
        ttl: Int
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val command = mutableListOf("ping", "-c", count.toString(), "-s", size.toString(), "-W", timeoutSec.toString())
                if (ttl != 64) {
                    command.addAll(listOf("-t", ttl.toString()))
                }
                command.add(host)

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

                if (!isActive) return@withContext false

                val errText = errorReader.readText()
                if (logs.isEmpty() && errText.isNotEmpty()) {
                    return@withContext false
                }

                if (rtts.isEmpty() && sent == 0) {
                    return@withContext false
                }

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
                return@withContext true
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    private fun constructIcmpEchoRequest(sequence: Int, payloadSize: Int): ByteArray {
        val headerSize = 8
        val buffer = ByteBuffer.allocate(headerSize + payloadSize)
        
        buffer.put(8.toByte()) // Type: Echo Request (8)
        buffer.put(0.toByte()) // Code: 0
        buffer.putShort(0.toShort()) // Checksum placeholder
        buffer.putShort(0x1234.toShort()) // Identifier
        buffer.putShort(sequence.toShort()) // Sequence number
        
        for (i in 0 until payloadSize) {
            buffer.put(0.toByte())
        }
        
        val packet = buffer.array()
        val checksum = calculateChecksum(packet)
        
        packet[2] = ((checksum.toInt() shr 8) and 0xFF).toByte()
        packet[3] = (checksum.toInt() and 0xFF).toByte()
        
        return packet
    }

    private fun calculateChecksum(buf: ByteArray): Short {
        var length = buf.size
        var i = 0
        var sum = 0
        
        while (length > 1) {
            val first = buf[i].toInt() and 0xFF
            val second = buf[i + 1].toInt() and 0xFF
            sum += (first shl 8) or second
            i += 2
            length -= 2
        }
        if (length > 0) {
            val first = buf[i].toInt() and 0xFF
            sum += (first shl 8)
        }
        
        while ((sum shr 16) != 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        
        return sum.inv().toShort()
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
