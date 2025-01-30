package com.immon.truckorbit.ui.fragments.driver

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.immon.truckorbit.R
import com.immon.truckorbit.TruckOrbit.getAppContext
import com.immon.truckorbit.data.Constants.TRUCK_DATABASE
import com.immon.truckorbit.data.Constants.USER_DATABASE
import com.immon.truckorbit.data.enums.DrivingStatusModel
import com.immon.truckorbit.data.models.TruckModel
import com.immon.truckorbit.data.models.UserModel
import com.immon.truckorbit.databinding.FragmentLocationSharingBinding
import com.immon.truckorbit.ui.fragments.base.BaseFragment
import com.immon.truckorbit.utils.AnimationQueue
import com.immon.truckorbit.utils.applyWindowInsets
import java.util.UUID

class LocationSharingFragment : BaseFragment() {

    private lateinit var binding: FragmentLocationSharingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var animationQueue: AnimationQueue
    private lateinit var selectedTruckId: String
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

    override val isLightStatusbar: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedTruckId = arguments?.getString("selectedTruckId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationSharingBinding.inflate(inflater, container, false)

        (binding.btnStartDriving.parent as ViewGroup).applyWindowInsets(top = false, bottom = true)

        setButtonVisibility()

        askLocationPermission()

        binding.btnStartDriving.setOnClickListener {
            updateDrivingStatus(true)
        }

        binding.btnStopDriving.setOnClickListener {
            updateDrivingStatus(false)
        }

        return binding.root
    }

    private fun setButtonVisibility() {
        if (::selectedTruckId.isInitialized.not()) {
            Log.e("Firestore", "No truck selected")
            return
        }

        val truckDocRef = firestore.collection(TRUCK_DATABASE).document(selectedTruckId)

        truckDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val drivingStatus = document.getString("drivingStatus")

                    if (drivingStatus == DrivingStatusModel.IDLE.name) {
                        binding.btnStartDriving.visibility = View.VISIBLE
                        binding.btnStopDriving.visibility = View.INVISIBLE
                    } else {
                        binding.btnStartDriving.visibility = View.INVISIBLE
                        binding.btnStopDriving.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("Firestore", "Truck document not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to fetch truck data: ${exception.message}", exception)
            }
    }

    private fun updateDrivingStatus(isDriving: Boolean) {
        if (::selectedTruckId.isInitialized.not()) {
            Toast.makeText(requireContext(), "No truck selected", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnStartDriving.startAnimation()
        binding.btnStopDriving.startAnimation()

        val truckDocRef = firestore.collection(TRUCK_DATABASE).document(selectedTruckId)

        firestore.runTransaction { transaction ->
            val truckSnapshot = transaction.get(truckDocRef)
            val truckModel = truckSnapshot.toObject(TruckModel::class.java)
                ?: throw IllegalStateException("Truck not found")

            val userDocRef = truckModel.currentDriver?.let {
                firestore.collection(USER_DATABASE).document(it.id)
            } ?: throw IllegalStateException("No driver assigned to this truck")

            val userSnapshot = transaction.get(userDocRef)
            val userModel = userSnapshot.toObject(UserModel::class.java)
                ?: throw IllegalStateException("User not found")

            truckModel.drivingStatus =
                if (isDriving) DrivingStatusModel.DRIVING else DrivingStatusModel.STOPPED
            userModel.drivingStatus = truckModel.drivingStatus

            if (!isDriving) {
                truckModel.currentDriver = null
                truckModel.destination = null
            }

            transaction.set(truckDocRef, truckModel)
            transaction.set(userDocRef, userModel)
        }.addOnSuccessListener {
            val statusText = if (isDriving) "driving" else "stopped"
            Toast.makeText(
                requireContext(),
                "Truck is now $statusText",
                Toast.LENGTH_SHORT
            ).show()

            if (isDriving) {
                binding.btnStartDriving.visibility = View.INVISIBLE
                binding.btnStopDriving.visibility = View.VISIBLE
            } else {
                binding.btnStartDriving.visibility = View.INVISIBLE
                binding.btnStopDriving.visibility = View.INVISIBLE
                requireActivity().onBackPressed()
            }

            binding.btnStartDriving.revertAnimation()
            binding.btnStopDriving.revertAnimation()
        }.addOnFailureListener { exception ->
            Toast.makeText(
                requireContext(),
                "Failed to update status: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("Firestore", "Failed to update truck status: ${exception.message}", exception)

            binding.btnStartDriving.revertAnimation()
            binding.btnStopDriving.revertAnimation()
        }
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
            uiSettings.isZoomControlsEnabled = false
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

            saveTruckLocation(latlng)

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
                markerLoadedFirstTime = false
            } else {
                animationQueue.addToQueue(latlng, threshold = 0f)
            }

            lastLocation = latlng

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 15f)
            googleMap!!.animateCamera(cameraUpdate)
        }
    }

    private fun saveTruckLocation(latLng: LatLng) {
        if (::selectedTruckId.isInitialized.not()) {
            Toast.makeText(requireContext(), "No truck selected", Toast.LENGTH_SHORT).show()
            return
        }

        val truckDocRef = firestore.collection(TRUCK_DATABASE).document(selectedTruckId)

        val locationMap = mapOf(
            "location" to mapOf(
                "id" to UUID.randomUUID().toString(),
                "latitude" to latLng.latitude,
                "longitude" to latLng.longitude
            )
        )

        truckDocRef.update(locationMap)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}