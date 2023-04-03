package com.example.tourmate.ui.nearby

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.example.tourmate.R
import com.example.tourmate.databinding.FragmentNearbyBinding
import com.example.tourmate.utilities.Constants.Companion.GOOGLE_MAPS_API_KEY
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

class NearbyFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLatLong = LatLng(0.0, 0.0)
    private var currentLat = 0.0
    private var currentLong = 0.0


    private val binding by lazy {
        FragmentNearbyBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby, container, false)
    }

    companion object {
        fun newInstance() = NearbyFragment()
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        setupMap()
    }

    private fun setupMap() {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                lastLocation = it
                currentLat = it.latitude
                currentLong = it.longitude
                currentLatLong = LatLng(it.latitude, it.longitude)
                placeMarkerOnMap(currentLatLong)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 20f))
            }
        }
    }


    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        map.addMarker(markerOptions)

    }

    override fun onMarkerClick(p0: Marker) = false
    private fun getNearByPlace(placeType: String) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + //Url
                "?location=" + currentLat + "," + currentLong +
                "&radius=5000" + "&type=" + placeType + "&sensor=true" +
                "&key=" + GOOGLE_MAPS_API_KEY

    }

}