package com.example.tourmate.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tourmate.R
import com.example.tourmate.databinding.ActivityMainBinding
import com.example.tourmate.ui.home.HomeFragment
import com.example.tourmate.ui.nearby.NearbyFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationBarView
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val menuIds = listOf(R.id.menu_home, R.id.menu_nearby, R.id.menu_setting)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bottomNavigationView.setOnItemSelectedListener(this)
        binding.pager.adapter = pagerAdapter
        binding.pager.isUserInputEnabled = false
    }
    private val pagerAdapter = PagerAdapter(this)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.pager.currentItem = menuIds.indexOf(item.itemId)
        return true
    }
    companion object {
        val listFragmentMenu = listOf<Class<*>>(
            HomeFragment::class.java,
            NearbyFragment::class.java,
        )
    }
    inner class PagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return menuIds.size
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment.newInstance()
                1 -> NearbyFragment.newInstance()

                else -> HomeFragment.newInstance()
            }
        }
    }
//    private lateinit var map: GoogleMap
//    private lateinit var lastLocation: Location
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//    }
//
//    override fun onMapReady(p0: GoogleMap) {
//        map = p0
//        map.uiSettings.isZoomControlsEnabled = true
//        map.setOnMarkerClickListener(this)
//        setupMap()
//    }
//
//    private fun setupMap() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
//            return
//        }
//        map.isMyLocationEnabled = true
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this){
//            if(it != null){
//                lastLocation = it
//                val currentLatLong = LatLng(it.latitude, it.longitude)
//                placeMarkerOnMap(currentLatLong)
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 20f))
//            }
//        }
//    }
//
//    private fun placeMarkerOnMap(currentLatLong: LatLng) {
//        val markerOptions = MarkerOptions().position(currentLatLong)
//        markerOptions.title("$currentLatLong")
//        map.addMarker(markerOptions)
//
//    }
//
//    override fun onMarkerClick(p0: Marker) = false
//    companion object{
//        private const val LOCATION_REQUEST_CODE = 1
//    }

}
