package com.example.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class ConnectivityStatus {
    Available, Unavailable
}

class NetworkConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val statusFlow: Flow<ConnectivityStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectivityStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityStatus.Unavailable)
            }

            override fun onUnavailable() {
                trySend(ConnectivityStatus.Unavailable)
            }
        }

        var registered = false
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, callback)
            registered = true
        } catch (e: Exception) {
            // Fallback gracefully if network callbacks are restricted
        }

        // Initial check
        val initialStatus = try {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                ConnectivityStatus.Available
            } else {
                ConnectivityStatus.Unavailable
            }
        } catch (e: Exception) {
            ConnectivityStatus.Available // Safe baseline fallback
        }
        trySend(initialStatus)

        awaitClose {
            if (registered) {
                try {
                    connectivityManager?.unregisterNetworkCallback(callback)
                } catch (e: Exception) {
                    // Clear callback gracefully
                }
            }
        }
    }.distinctUntilChanged()
}
