package page.gagerandall.netpulse.ui.screens.whois

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.regex.Pattern

class WhoisViewModel : ViewModel() {

    data class WhoisDetails(
        val registrar: String = "Unknown",
        val registrantOrg: String = "Unknown",
        val creationDate: String = "Unknown",
        val expiryDate: String = "Unknown",
        val nameServers: List<String> = emptyList(),
        val rawResponse: String = "",
        val status: String = "Idle", // Idle, running, complete, failed
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(WhoisDetails())
    val state: StateFlow<WhoisDetails> = _state

    private var whoisJob: Job? = null

    fun stopWhois() {
        whoisJob?.cancel()
        whoisJob = null
        _state.value = WhoisDetails(status = "Failed", errorMessage = "Query stopped by user.")
    }

    fun queryWhois(queryStr: String) {
        _state.value = WhoisDetails(status = "Running")
        whoisJob = viewModelScope.launch(Dispatchers.IO) {
            val q = queryStr.trim().lowercase()
            if (q.isEmpty()) {
                _state.value = WhoisDetails(status = "Failed", errorMessage = "WHOIS query input is empty.")
                return@launch
            }

            try {
                // Step 1: Query whois.iana.org on port 43
                var currentServer = "whois.iana.org"
                var resultText = executeWhoisSocketQuery(currentServer, q)

                if (!isActive) return@launch

                // Step 2: Check if there's a refer: server returned (e.g. refer: whois.verisign-grs.com)
                val referServer = parseReferServer(resultText)
                if (referServer != null && referServer != currentServer) {
                    currentServer = referServer
                    // Query the referred authoritative WHOIS server to get full registration info
                    val detailedResult = executeWhoisSocketQuery(currentServer, q)
                    if (!isActive) return@launch
                    if (detailedResult.isNotEmpty()) {
                        resultText = detailedResult
                    }
                }

                // Parse key elements via Regex
                val parsedDetails = parseWhoisInformation(resultText)
                
                if (!isActive) return@launch

                _state.value = parsedDetails.copy(
                    rawResponse = resultText,
                    status = "Complete"
                )

            } catch (e: Exception) {
                _state.value = WhoisDetails(
                    status = "Failed",
                    errorMessage = e.message ?: "Failed connecting to WHOIS registration registrar servers."
                )
            }
        }
    }

    private fun executeWhoisSocketQuery(server: String, query: String): String {
        return try {
            val s = Socket(server, 43)
            s.soTimeout = 8000
            val writer = PrintWriter(s.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(s.getInputStream()))

            // send query
            writer.println(query)

            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append("\n")
            }

            s.close()
            builder.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseReferServer(raw: String): String? {
        val pattern = Pattern.compile("(?i)refer:\\s*([a-zA-Z0-9.-]+)")
        val matcher = pattern.matcher(raw)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }
        val patternWhois = Pattern.compile("(?i)whois server:\\s*([a-zA-Z0-9.-]+)")
        val matcherWhois = patternWhois.matcher(raw)
        if (matcherWhois.find()) {
            return matcherWhois.group(1)?.trim()
        }
        return null
    }

    private fun parseWhoisInformation(raw: String): WhoisDetails {
        var registrar = "Unknown"
        var registrantOrg = "Unknown"
        var creationDate = "Unknown"
        var expiryDate = "Unknown"
        val nameServers = mutableListOf<String>()

        val rawLines = raw.split("\n")
        rawLines.forEach { line ->
            val cleanLine = line.trim()
            val lower = cleanLine.lowercase()
            when {
                lower.startsWith("registrar:") -> {
                    registrar = cleanLine.substringAfter(":").trim()
                }
                lower.startsWith("registrant organization:") || lower.startsWith("org:") -> {
                    registrantOrg = cleanLine.substringAfter(":").trim()
                }
                lower.startsWith("creation date:") || lower.startsWith("created:") || lower.startsWith("registered:") -> {
                    creationDate = cleanLine.substringAfter(":").trim()
                }
                lower.startsWith("registry expiry date:") || lower.startsWith("expiry date:") || lower.startsWith("expire:") || lower.startsWith("expires:") -> {
                    expiryDate = cleanLine.substringAfter(":").trim()
                }
                lower.startsWith("name server:") || lower.startsWith("nserver:") -> {
                    val ns = cleanLine.substringAfter(":").trim().lowercase()
                    if (ns.isNotEmpty() && !nameServers.contains(ns)) {
                        nameServers.add(ns)
                    }
                }
            }
        }

        // If REGEX failed on registrar, match some custom common domains whois patterns
        if (registrar == "Unknown") {
            val matcherReg = Pattern.compile("Registrar WHOIS Server:\\s*(.*)").matcher(raw)
            if (matcherReg.find()) {
                registrar = matcherReg.group(1)?.trim() ?: "Unknown"
            }
        }

        return WhoisDetails(
            registrar = registrar,
            registrantOrg = registrantOrg,
            creationDate = creationDate,
            expiryDate = expiryDate,
            nameServers = nameServers
        )
    }
}
