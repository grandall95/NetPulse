package page.gagerandall.netpulse.ui.screens.portscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.InetSocketAddress
import java.net.Socket

class PortScannerViewModel : ViewModel() {

    enum class PortStatus {
        OPEN, CLOSED, FILTERED
    }

    data class PortScanResult(
        val port: Int,
        val service: String,
        val status: PortStatus
    )

    data class ScanState(
        val status: String = "Idle", // Idle, running, complete, failed
        val progress: Float = 0f, // 0.0 to 1.0
        val scannedPorts: List<PortScanResult> = emptyList(),
        val elapsedMs: Long = 0,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state

    private var scanJob: Job? = null

    fun clear() {
        _state.value = ScanState()
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _state.value = ScanState(
            status = "Failed",
            errorMessage = "Port scan stopped by user."
        )
    }

    private val portServices = mapOf(
        21 to "FTP",
        22 to "SSH",
        23 to "Telnet",
        25 to "SMTP",
        53 to "DNS",
        80 to "HTTP",
        110 to "POP3",
        143 to "IMAP",
        443 to "HTTPS",
        445 to "SMB",
        1433 to "MSSQL",
        1521 to "OracleDB",
        3306 to "MySQL",
        3389 to "RDP",
        5432 to "PostgreSQL",
        6379 to "Redis",
        8080 to "HTTP-Alt",
        27017 to "MongoDB"
    )

    fun startScan(
        host: String,
        portsToScan: List<Int>,
        timeoutMs: Int = 200,
        concurrencyLimit: Int = 50
    ) {
        _state.value = ScanState(status = "Running", progress = 0.0f)
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            val cleanedHost = host.trim()
            if (cleanedHost.isEmpty()) {
                _state.value = ScanState(status = "Failed", errorMessage = "IP Address or Host domain is blank.")
                return@launch
            }

            val startTime = System.currentTimeMillis()
            val totalPorts = portsToScan.size
            if (totalPorts == 0) {
                _state.value = ScanState(status = "Failed", errorMessage = "No target ports specified for scan.")
                return@launch
            }

            val semaphore = Semaphore(concurrencyLimit.coerceIn(1, 200))
            val resultsList = mutableListOf<PortScanResult>()
            var scannedCount = 0

            val scanJobs = portsToScan.map { port ->
                async {
                    if (!isActive) return@async
                    semaphore.withPermit {
                        if (!isActive) return@async
                        val status = probePort(cleanedHost, port, timeoutMs)
                        val serviceName = portServices[port] ?: "Unknown"
                        val result = PortScanResult(port, serviceName, status)

                        if (!isActive) return@async
                        synchronized(resultsList) {
                            resultsList.add(result)
                            scannedCount++
                            // Update progression state dynamically
                            if (isActive) {
                                _state.value = _state.value.copy(
                                    progress = scannedCount.toFloat() / totalPorts,
                                    scannedPorts = resultsList.sortedBy { it.port }
                                )
                            }
                        }
                    }
                }
            }

            try {
                scanJobs.awaitAll()
            } catch (e: Exception) {
                return@launch
            }

            if (!isActive) return@launch
            val elapsed = System.currentTimeMillis() - startTime

            _state.value = _state.value.copy(
                status = "Complete",
                progress = 1.0f,
                elapsedMs = elapsed
            )
        }
    }

    private fun probePort(host: String, port: Int, timeoutMs: Int): PortStatus {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeoutMs)
            socket.close()
            PortStatus.OPEN
        } catch (e: java.net.SocketTimeoutException) {
            PortStatus.FILTERED
        } catch (e: Exception) {
            PortStatus.CLOSED
        }
    }
}
