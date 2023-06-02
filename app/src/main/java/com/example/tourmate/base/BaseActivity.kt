package com.example.tourmate.base

import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.tourmate.R
import com.example.tourmate.controller.*
import com.example.tourmate.controller.MyHistoryActivity
import com.example.tourmate.controller.broadcastReceiver.LocationBroadcastReceiver
import com.example.tourmate.controller.broadcastReceiver.NetworkChangeReceiver
import com.example.tourmate.controller.map.NearbyActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


open class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var networkChangeReceiver: NetworkChangeReceiver? = null
    private var locationBroadcastReceiver: LocationBroadcastReceiver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerNetworkChangeReceiver()
        registerLocationChangReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChangeReceiver()
        unregisterLocationChangeReceiver()
    }

    private fun registerNetworkChangeReceiver() {
        networkChangeReceiver = NetworkChangeReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }
    private fun registerLocationChangReceiver(){
        locationBroadcastReceiver = LocationBroadcastReceiver()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationBroadcastReceiver, filter)
    }
    private fun unregisterNetworkChangeReceiver() {
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver)
        }
    }
    private fun unregisterLocationChangeReceiver() {
        if (locationBroadcastReceiver != null) {
            unregisterReceiver(locationBroadcastReceiver)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.favorite -> {
                val intent = Intent(this, MyFavoriteActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.saved_place -> {
                val intent = Intent(this, SavedPlaceActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.nearby -> {
                val intent = Intent(this, NearbyActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.log_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.history ->{
                val intent = Intent(this, MyHistoryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }    }
}
