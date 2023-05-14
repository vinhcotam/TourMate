package com.example.tourmate.base

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.tourmate.network.NetworkChangeReceiver


open class BaseActivity : AppCompatActivity() {
    private var networkChangeReceiver: NetworkChangeReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerNetworkChangeReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChangeReceiver()
    }

    private fun registerNetworkChangeReceiver() {
        networkChangeReceiver = NetworkChangeReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }

    private fun unregisterNetworkChangeReceiver() {
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }
}
