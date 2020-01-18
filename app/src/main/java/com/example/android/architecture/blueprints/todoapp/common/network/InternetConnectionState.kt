package com.example.android.architecture.blueprints.todoapp.common.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Interface for querying internet connection state
 */
interface InternetConnection {
    /**
     * Suspends execution until internet connection is available
     */
    suspend fun waitForInternet()
}

/**
 * Implementation of [InternetConnection] that uses Android's [ConnectivityManager] to query for available
 * internet connection.
 *
 * Execution is suspended until [ConnectivityManager.NetworkCallback] onAvailable() is called.
 *
 * @param context instance of [Context]
 */
class InternetConnectionState @Inject constructor(
        context: Context
) : InternetConnection {
    private val appContext: Context = context.applicationContext

    override suspend fun waitForInternet(): Unit = suspendCancellableCoroutine { continuation ->
        val connectivityManager = ContextCompat.getSystemService(appContext, ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) {
                Timber.d("Internet available")
                connectivityManager?.unregisterNetworkCallback(this)
                continuation.resume(Unit)
            }
        }
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
        continuation.invokeOnCancellation { connectivityManager?.unregisterNetworkCallback(networkCallback) }
    }
}