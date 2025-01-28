package com.immon.truckorbit.ui.fragments.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.immon.truckorbit.data.Constants.TRUCK_DATABASE
import com.immon.truckorbit.data.Constants.USER_DATABASE
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.data.enums.DrivingStatusModel
import com.immon.truckorbit.data.models.TruckModel
import com.immon.truckorbit.data.models.UserModel
import com.immon.truckorbit.databinding.FragmentHomeBinding
import com.immon.truckorbit.ui.fragments.base.BaseFragment

class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var selectedTruckId: String? = null
    private var currentUserId = firebaseAuth.currentUser?.uid

    override val isLightStatusbar: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalDB.putBoolean("logged_in", true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        fetchTrucks { truckList ->
            val truckNames = truckList.map { it.truckName }
            binding.spinnerTrucks.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                truckNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            binding.spinnerTrucks.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedTruckId = truckList[position].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedTruckId = null
                    }
                }
        }

        binding.btnGetStarted.setOnClickListener {
            binding.btnGetStarted.startAnimation {
                assignDrivingStatus()
            }
        }

        return binding.root
    }

    private fun fetchTrucks(callback: (List<TruckModel>) -> Unit) {
        firestore.collection(TRUCK_DATABASE).get()
            .addOnSuccessListener { result ->
                val truckList = result.documents.mapNotNull { it.toObject<TruckModel>() }
                selectedTruckId = truckList.firstOrNull()?.id
                callback(truckList)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun assignDrivingStatus() {
        if (selectedTruckId == null) {
            Toast.makeText(
                requireContext(),
                "Please select a truck first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (binding.etDestination.text.toString().trim().isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter a destination",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val truckDocRef = firestore.collection(TRUCK_DATABASE).document(selectedTruckId!!)
        val userDocRef = firestore.collection(USER_DATABASE).document(currentUserId!!)

        firestore.runTransaction { transaction ->
            val truckSnapshot = transaction.get(truckDocRef)
            val truckModel = truckSnapshot.toObject<TruckModel>()
                ?: throw IllegalStateException("Truck not found")

            val userSnapshot = transaction.get(userDocRef)
            val userModel = userSnapshot.toObject<UserModel>()
                ?: throw IllegalStateException("User not found")

            if (truckModel.currentDriver != null) {
                Toast.makeText(
                    requireContext(),
                    "Truck is already assigned to another driver",
                    Toast.LENGTH_SHORT
                ).show()
                return@runTransaction
            }

            if (truckModel.drivingStatus != DrivingStatusModel.IDLE) {
                Toast.makeText(
                    requireContext(),
                    "Truck is already moving",
                    Toast.LENGTH_SHORT
                ).show()
                return@runTransaction
            }

            if (userModel.drivingStatus != DrivingStatusModel.IDLE) {
                Toast.makeText(
                    requireContext(),
                    "User is already driving",
                    Toast.LENGTH_SHORT
                ).show()
                return@runTransaction
            }

            truckModel.currentDriver = userModel
            truckModel.drivingStatus = DrivingStatusModel.IDLE

            userModel.drivingStatus = DrivingStatusModel.IDLE

            transaction.set(truckDocRef, truckModel)
            transaction.set(userDocRef, userModel)
        }.addOnSuccessListener {
            binding.btnGetStarted.revertAnimation()
            Toast.makeText(
                requireContext(),
                "Truck assigned to driver successfully",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { exception ->
            binding.btnGetStarted.revertAnimation()
            exception.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Failed to assign truck: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}