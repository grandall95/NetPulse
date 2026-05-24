package com.example.ui.screens.subnet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.SubnetCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubnetCalculatorViewModel : ViewModel() {

    data class SubnetState(
        val ipAddress: String = "192.168.1.1",
        val cidr: Int = 24,
        val details: SubnetCalculator.SubnetDetails? = null,
        val splitSubnets: List<Pair<String, Int>> = emptyList(),
        val splitCount: Int = 4,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(SubnetState())
    val state: StateFlow<SubnetState> = _state

    init {
        recalculateSubnet("192.168.1.1", 24, 4)
    }

    fun recalculateSubnet(ip: String, cidr: Int, splits: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val ipStr = ip.trim()
                if (ipStr.isEmpty()) {
                    _state.value = _state.value.copy(errorMessage = "IP Address blank.")
                    return@launch
                }

                val result = SubnetCalculator.calculate(ipStr, cidr)
                val splitList = if (!result.isIpv6) {
                    SubnetCalculator.splitSubnetIpv4(result.networkAddress, cidr, splits)
                } else {
                    emptyList() // IPv6 split is simulated or skipped gracefully on details
                }

                _state.value = SubnetState(
                    ipAddress = ipStr,
                    cidr = cidr,
                    details = result,
                    splitSubnets = splitList,
                    splitCount = splits,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message ?: "Failed calculations."
                )
            }
        }
    }
}
