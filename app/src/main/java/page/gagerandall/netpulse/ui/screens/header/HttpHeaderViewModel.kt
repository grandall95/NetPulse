package page.gagerandall.netpulse.ui.screens.header

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

class HttpHeaderViewModel : ViewModel() {

    data class HeaderPair(val key: String, val value: String)
    data class RedirectNode(val url: String, val status: Int, val elapsedMs: Long)

    data class SslCertInfo(
        val subject: String = "Unknown",
        val issuer: String = "Unknown",
        val validFrom: String = "Unknown",
        val validTo: String = "Unknown",
        val sans: String = "None"
    )

    data class InspectorState(
        val status: String = "Idle", // Idle, running, complete, failed
        val statusCode: Int = 0,
        val responseHeaders: List<HeaderPair> = emptyList(),
        val requestHeadersSent: List<HeaderPair> = emptyList(),
        val responseTimeMs: Long = 0,
        val redirectChain: List<RedirectNode> = emptyList(),
        val sslInfo: SslCertInfo? = null,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(InspectorState())
    val state: StateFlow<InspectorState> = _state

    fun clear() {
        _state.value = InspectorState()
    }

    fun inspectUrl(
        urlInput: String,
        method: String = "GET",
        customHeaders: List<HeaderPair> = emptyList(),
        followRedirectsManually: Boolean = false
    ) {
        _state.value = InspectorState(status = "Running")
        viewModelScope.launch(Dispatchers.IO) {
            var targetUrl = urlInput.trim()
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                targetUrl = "https://$targetUrl"
            }

            // Create OkHttp client configured based on redirect option
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .followRedirects(!followRedirectsManually)
                .followSslRedirects(!followRedirectsManually)
                .build()

            val redirects = mutableListOf<RedirectNode>()
            var currentUrl = targetUrl
            var steps = 0
            val maxRedirectSteps = 10

            try {
                var response: Response? = null
                var lastStatus = 0
                var headersList = emptyList<HeaderPair>()
                var reqHeadersList = emptyList<HeaderPair>()
                var finalTime = 0L
                var sslCertDetails: SslCertInfo? = null

                while (steps < maxRedirectSteps) {
                    val requestBuilder = Request.Builder()
                        .url(currentUrl)
                        .method(method, null)

                    // Inject custom headers requested
                    customHeaders.forEach { pair ->
                        if (pair.key.isNotBlank()) {
                            requestBuilder.addHeader(pair.key, pair.value)
                        }
                    }

                    val request = requestBuilder.build()
                    val reqStartTime = System.currentTimeMillis()

                    response = client.newCall(request).execute()
                    val reqEndTime = System.currentTimeMillis()
                    val duration = reqEndTime - reqStartTime

                    lastStatus = response.code
                    headersList = convertHeadersToList(response.headers)
                    reqHeadersList = convertHeadersToList(response.request.headers)
                    finalTime = duration

                    // Grab SSL handshake info if https
                    if (currentUrl.startsWith("https://") && response.handshake != null) {
                        try {
                            val peerCerts = response.handshake?.peerCertificates
                            if (!peerCerts.isNullOrEmpty()) {
                                val cert = peerCerts[0] as? X509Certificate
                                if (cert != null) {
                                    sslCertDetails = SslCertInfo(
                                        subject = cert.subjectDN.name,
                                        issuer = cert.issuerDN.name,
                                        validFrom = cert.notBefore.toString(),
                                        validTo = cert.notAfter.toString(),
                                        sans = cert.subjectAlternativeNames?.joinToString(", ") { it.toString() } ?: "None"
                                    )
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    if (followRedirectsManually && (lastStatus in 300..399)) {
                        redirects.add(RedirectNode(currentUrl, lastStatus, duration))
                        val locationHeader = response.header("Location") ?: ""
                        if (locationHeader.isNotEmpty()) {
                            // Resolve relative URL redirects if needed
                            currentUrl = if (locationHeader.startsWith("http")) {
                                locationHeader
                            } else {
                                val baseUri = java.net.URI(currentUrl)
                                baseUri.resolve(locationHeader).toString()
                            }
                            steps++
                            response.close() // Close preceding response body
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }

                if (response != null) {
                    _state.value = InspectorState(
                        status = "Complete",
                        statusCode = lastStatus,
                        responseHeaders = headersList,
                        requestHeadersSent = reqHeadersList,
                        responseTimeMs = finalTime,
                        redirectChain = redirects.toList(),
                        sslInfo = sslCertDetails
                    )
                    response.close()
                } else {
                    throw IOException("Did not retrieve response payload.")
                }

            } catch (e: Exception) {
                _state.value = InspectorState(
                    status = "Failed",
                    errorMessage = e.message ?: "Failed negotiating connection to HTTP remote server."
                )
            }
        }
    }

    private fun convertHeadersToList(headers: Headers): List<HeaderPair> {
        val list = mutableListOf<HeaderPair>()
        for (i in 0 until headers.size) {
            list.add(HeaderPair(headers.name(i), headers.value(i)))
        }
        return list
    }
}
