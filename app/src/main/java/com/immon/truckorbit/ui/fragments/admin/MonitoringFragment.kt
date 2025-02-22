package com.immon.truckorbit.ui.fragments.admin

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.immon.truckorbit.R
import com.immon.truckorbit.TruckOrbit.getAppContext
import com.immon.truckorbit.data.Constants.TRUCK_DATABASE
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.data.enums.DrivingStatusModel
import com.immon.truckorbit.data.models.TruckModel
import com.immon.truckorbit.databinding.FragmentMonitoringBinding
import com.immon.truckorbit.ui.fragments.base.BaseFragment
import com.immon.truckorbit.utils.AnimationQueue
import com.immon.truckorbit.utils.DrawableUtils.drawableToBitmap

class MonitoringFragment : BaseFragment() {

    private lateinit var binding: FragmentMonitoringBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var animationQueue: AnimationQueue
    private val firestore = FirebaseFirestore.getInstance()
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var markerLoadedFirstTime = true
    private var lastLocation: LatLng? = null
    private val requestLocationPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasLocationPermission: Boolean ->
            startLocationUpdates()
            loadMapFragment(hasLocationPermission)
        }
    private val truckMarkers = mutableMapOf<String, Marker>()
    private val animationQueues = mutableMapOf<String, AnimationQueue>()

    override val isLightStatusbar: Boolean
        get() = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonitoringBinding.inflate(inflater, container, false)

        askLocationPermission()

        return binding.root
    }

    private fun askLocationPermission() {
        binding.progressBar.visibility = View.VISIBLE

        if (::locationRequest.isInitialized.not()) {
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(0)
                .setMaxUpdateDelayMillis(0)
                .build()
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())
            .addOnSuccessListener {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(requireActivity(), 1)
                        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        sendEx.printStackTrace()
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Unable to enable GPS.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun loadMapFragment(hasLocationPermission: Boolean) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?

        if (mapFragment != null && hasLocationPermission) {
            mapFragment.getMapAsync(OnMapReadyCallback { map: GoogleMap -> this.initializeMap(map) })
        }
    }

    private fun initializeMap(map: GoogleMap) {
        googleMap = map

        googleMap!!.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            isTrafficEnabled = false
            isIndoorEnabled = false
            isBuildingsEnabled = true
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMapToolbarEnabled = false
        }

        updateMapLocation(latLng = lastLocation)
    }

    private fun startLocationUpdates() {
        if (::fusedLocationClient.isInitialized.not()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getAppContext())
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        updateMapLocation(location = location)
                    }
                    listenForTruckUpdates()
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun updateMapLocation(location: Location? = null, latLng: LatLng? = null) {
        if (googleMap != null && (location != null || latLng != null)) {
            binding.progressBar.visibility = View.GONE

            val latlng = latLng ?: LatLng(location!!.latitude, location.longitude)

            if (currentMarker == null) {
                val markerOptions = MarkerOptions()
                    .position(latlng)
                    .title("Current Location")
                currentMarker = googleMap!!.addMarker(markerOptions)
            }

            if (markerLoadedFirstTime) {
                animationQueue = AnimationQueue(
                    startPosition = latlng,
                    scope = lifecycleScope,
                ) { updatedPosition ->
                    currentMarker?.position = updatedPosition
                }
            } else {
                animationQueue.addToQueue(latlng, threshold = 0f)
            }

            lastLocation = latlng

            if (markerLoadedFirstTime) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 12f)
                googleMap!!.animateCamera(cameraUpdate)
                markerLoadedFirstTime = false
            }
        }
    }

    private fun listenForTruckUpdates() {
        firestore.collection(TRUCK_DATABASE)
            .whereNotEqualTo("drivingStatus", DrivingStatusModel.STOPPED.name)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching truck data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                val truckList = mutableListOf<TruckModel>()
                val showIdleTrucks = LocalDB.getBoolean("show_idle_trucks", true)

                for (document in querySnapshot?.documents ?: emptyList()) {
                    val truck = document.toObject(TruckModel::class.java)
                    if (truck?.location != null) {
                        if (!showIdleTrucks && truck.drivingStatus == DrivingStatusModel.IDLE) continue

                        truckList.add(truck)
                    }
                }
                updateTruckMarkersOnMap(truckList)
            }
    }

    private fun updateTruckMarkersOnMap(truckList: List<TruckModel>) {
        if (context == null) return

        val drawable = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.ic_truck_marker
        )
        val bitmap = drawable.drawableToBitmap()

        truckList.forEach { truck ->
            truck.location?.let { latLng ->
                val latlng = LatLng(latLng.latitude, latLng.longitude)
                val markerOptions = MarkerOptions()
                    .position(latlng)
                    .title(truck.truckName)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))

                var truckMarker = truckMarkers[truck.id]

                if (truckMarker == null) {
                    truckMarker = googleMap?.addMarker(markerOptions)
                    truckMarker?.tag = truck.id
                    truckMarker?.let {
                        truckMarkers[truck.id] = it
                    }
                }

                if (animationQueues[truck.id] == null) {
                    animationQueues[truck.id] = AnimationQueue(
                        startPosition = latlng,
                        scope = lifecycleScope,
                    ) { updatedPosition ->
                        truckMarker?.position = updatedPosition
                    }
                } else {
                    animationQueues[truck.id]!!.addToQueue(latlng, threshold = 0f)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}