package page.gagerandall.netpulse.ui.screens.dns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xbill.DNS.*
import org.xbill.DNS.Record
import java.net.InetAddress

class DnsLookupViewModel : ViewModel() {

    data class DnsRecordResult(
        val value: String,
        val ttl: Long = 0,
        val type: String = "A"
    )

    data class DnsQueryState(
        val status: String = "Idle", // Idle, running, complete, failed
        val records: List<DnsRecordResult> = emptyList(),
        val rawResponse: String = "",
        val responseTimeMs: Long = 0,
        val authoritativeFlag: Boolean = false,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(DnsQueryState())
    val state: StateFlow<DnsQueryState> = _state

    fun lookupDns(
        domain: String,
        recordTypeStr: String,
        useCustomServer: Boolean = false,
        customServerIp: String = "1.1.1.1"
    ) {
        _state.value = DnsQueryState(status = "Running")
        viewModelScope.launch(Dispatchers.IO) {
            val cleanedDomain = domain.trim().lowercase()
            if (cleanedDomain.isEmpty()) {
                _state.value = DnsQueryState(status = "Failed", errorMessage = "Domain input cannot be blank.")
                return@launch
            }

            // Convert record type str to dnsjava Type int
            val typeInt = when (recordTypeStr.uppercase()) {
                "A" -> Type.A
                "AAAA" -> Type.AAAA
                "MX" -> Type.MX
                "TXT" -> Type.TXT
                "CNAME" -> Type.CNAME
                "NS" -> Type.NS
                "PTR" -> Type.PTR
                else -> Type.A
            }

            val startTime = System.currentTimeMillis()

            try {
                if (!useCustomServer) {
                    // Simple path: resolve via system InetAddress or default Lookup
                    val records = mutableListOf<DnsRecordResult>()
                    
                    if (typeInt == Type.A || typeInt == Type.AAAA) {
                        val addresses = InetAddress.getAllByName(cleanedDomain)
                        addresses.forEach { addr ->
                            val currentType = if (addr.hostAddress?.contains(":") == true) "AAAA" else "A"
                            if (recordTypeStr.uppercase() == "A" && currentType == "A") {
                                records.add(DnsRecordResult(addr.hostAddress ?: "", 0, "A"))
                            } else if (recordTypeStr.uppercase() == "AAAA" && currentType == "AAAA") {
                                records.add(DnsRecordResult(addr.hostAddress ?: "", 0, "AAAA"))
                            }
                        }
                    }

                    // For the Simple tab system lookup, if records are still empty and we can use Lookup from dnsjava
                    // we can fetch it via standard resolution to provide detailed CNAME/MX results natively!
                    if (records.isEmpty()) {
                        val lookup = Lookup(cleanedDomain, typeInt)
                        val queryResults = lookup.run()

                        if (queryResults != null) {
                            queryResults.forEach { rec ->
                                records.add(DnsRecordResult(rec.rdataToString(), rec.ttl, recordTypeStr))
                            }
                        }
                    }

                    val elapsed = System.currentTimeMillis() - startTime
                    _state.value = DnsQueryState(
                        status = "Complete",
                        records = records,
                        responseTimeMs = elapsed,
                        rawResponse = "System resolved: ${records.size} record(s) fetched."
                    )
                } else {
                    // Custom DNS path: use Custom Server via SimpleResolver from dnsjava
                    val ipStr = customServerIp.trim()
                    val dnsResolver = SimpleResolver(ipStr.ifEmpty { "1.1.1.1" })
                    
                    val name = Name.fromString(if (cleanedDomain.endsWith(".")) cleanedDomain else "$cleanedDomain.")
                    val queryRecord = Record.newRecord(name, typeInt, DClass.IN)
                    val queryMessage = Message.newQuery(queryRecord)

                    // Execute query
                    val responseMessage = dnsResolver.send(queryMessage)
                    val elapsed = System.currentTimeMillis() - startTime

                    val responseAnswers = responseMessage.getSection(Section.ANSWER)
                    val recordsList = responseAnswers.map { rec ->
                        DnsRecordResult(
                            value = rec.rdataToString() ?: "",
                            ttl = rec.ttl,
                            type = Type.string(rec.type) ?: "A"
                        )
                    }

                    // Gather Authoritative Flag
                    val authHeader = responseMessage.header.getFlag(Flags.AA.toInt())

                    _state.value = DnsQueryState(
                        status = "Complete",
                        records = recordsList,
                        responseTimeMs = elapsed,
                        authoritativeFlag = authHeader,
                        rawResponse = responseMessage.toString()
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    status = "Failed",
                    errorMessage = e.message ?: "Failed DNS record lookup sequence."
                )
            }
        }
    }
}
