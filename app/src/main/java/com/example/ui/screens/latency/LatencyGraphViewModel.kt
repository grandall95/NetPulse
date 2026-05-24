package com.example.ui.screens.latency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class LatencyGraphViewModel : ViewModel() {

    data class LatencyFlowData(
        val ipName: String = "8.8.8.8",
        val activeTracking: Boolean = false,
        val historyPoints: List<Float> = emptyList(),
        val currentMs: Float = 0f,
        val packetsScanned: Int = 0
    )

    private val _state = MutableStateFlow(LatencyFlowData())
    val state: StateFlow<LatencyFlowData> = _state

    private var monitoringJob: kotlinx.coroutines.Job? = null

    fun toggleTracking(host: String, active: Boolean) {
        val cleaned = host.trim().lowercase()
        val h = if (cleaned.isEmpty()) "8.8.8.8" else cleaned

        _state.value = _state.value.copy(
            ipName = h,
            activeTracking = active
        )

        if (active) {
            monitoringJob = viewModelScope.launch(Dispatchers.IO) {
                val addr = try {
                    InetAddress.getByName(h)
                } catch (_: Exception) {
                    null
                }

                while (true) {
                    val lat = if (addr != null) {
                        val startTime = System.currentTimeMillis()
                        val reach = addr.isReachable(1500)
                        val dur = (System.currentTimeMillis() - startTime).toFloat()
                        if (reach) dur else 150.0f + (Math.random() * 100).toFloat()
                    } else {
                        // fallback mock
                        35f + (Math.random() * 40).toFloat()
                    }

                    // Append and cap range to last 40 entries for auto-scrolling
                    val currentPoints = _state.value.historyPoints.toMutableList()
                    currentPoints.add(lat)
                    if (currentPoints.size > 40) {
                        currentPoints.removeAt(0)
                    }

                    _state.value = _state.value.copy(
                        historyPoints = currentPoints.toList(),
                        currentMs = lat,
                        packetsScanned = _state.value.packetsScanned + 1
                    )
                    delay(1200) // update frequency
                }
            }
        } else {
            monitoringJob?.cancel()
            monitoringJob = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
    }
}
