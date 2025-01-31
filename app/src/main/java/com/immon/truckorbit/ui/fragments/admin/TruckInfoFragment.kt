package com.immon.truckorbit.ui.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.immon.truckorbit.R
import com.immon.truckorbit.data.Constants.TRUCK_DATABASE
import com.immon.truckorbit.data.enums.DrivingStatusModel
import com.immon.truckorbit.data.models.TruckModel
import com.immon.truckorbit.databinding.FragmentTruckInfoBinding
import com.immon.truckorbit.ui.fragments.base.BaseFragment
import com.immon.truckorbit.utils.AnimationQueue
import com.immon.truckorbit.utils.DrawableUtils.drawableToBitmap
import com.immon.truckorbit.utils.setTitle

class TruckInfoFragment : BaseFragment() {

    private lateinit var binding: FragmentTruckInfoBinding
    private lateinit var animationQueue: AnimationQueue
    private val firestore = FirebaseFirestore.getInstance()
    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var markerLoadedFirstTime = true
    private var lastLocation: LatLng? = null
    private lateinit var truckId: String

    override val isLightStatusbar: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            truckId = it.getString("truckId", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTruckInfoBinding.inflate(inflater, container, false)

        binding.header.toolbar.setTitle(requireContext(), "Truck Info", true)

        loadMapFragment()

        return binding.root
    }

    private fun loadMapFragment() {
        binding.progressBar.visibility = View.VISIBLE

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?

        mapFragment?.getMapAsync { map: GoogleMap -> this.initializeMap(map) }
    }

    private fun initializeMap(map: GoogleMap) {
        googleMap = map

        googleMap!!.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            isTrafficEnabled = true
            isIndoorEnabled = false
            isBuildingsEnabled = true
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMapToolbarEnabled = false
        }

        listenForTruckUpdates()
    }

    private fun listenForTruckUpdates() {
        firestore.collection(TRUCK_DATABASE).document(truckId)
            .addSnapshotListener { snapshot, exception ->
                binding.progressBar.visibility = View.GONE

                if (exception != null) {
                    exception.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val truck = snapshot.toObject<TruckModel>()

                    binding.tvTruckName.text = truck?.truckName ?: "N/A"
                    binding.tvLicenseNo.text = truck?.licenseNo ?: "N/A"
                    binding.tvDriverName.text = truck?.currentDriver?.name ?: "N/A"
                    binding.tvDriverContact.text = truck?.currentDriver?.email ?: "N/A"
                    binding.tvDriverStatus.text = when (truck?.drivingStatus) {
                        DrivingStatusModel.DRIVING -> "Moving"
                        DrivingStatusModel.IDLE -> "Idle"
                        DrivingStatusModel.STOPPED -> "Stopped"
                        else -> "N/A"
                    }
                    binding.tvGoingTo.text = truck?.destination ?: "N/A"

                    val latLng = if (truck?.location != null) {
                        LatLng(truck.location!!.latitude, truck.location!!.longitude)
                    } else {
                        null
                    }

                    updateMapLocation(latLng)
                }
            }
    }

    private fun updateMapLocation(latLng: LatLng? = null) {
        if (googleMap != null && latLng != null) {
            binding.progressBar.visibility = View.GONE

            val drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_truck_marker
            )
            val bitmap = drawable.drawableToBitmap()

            if (currentMarker == null) {
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                currentMarker = googleMap!!.addMarker(markerOptions)
            }

            if (markerLoadedFirstTime) {
                animationQueue = AnimationQueue(
                    startPosition = latLng,
                    scope = lifecycleScope,
                ) { updatedPosition ->
                    currentMarker?.position = updatedPosition
                }
            } else {
                animationQueue.addToQueue(latLng, threshold = 0f)
            }

            lastLocation = latLng

            if (markerLoadedFirstTime) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                googleMap!!.animateCamera(cameraUpdate)
                markerLoadedFirstTime = false
            }
        }
    }
}