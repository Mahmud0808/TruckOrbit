package com.immon.truckorbit.ui.fragments.admin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.immon.truckorbit.R
import com.immon.truckorbit.databinding.FragmentTrucksBinding
import com.immon.truckorbit.ui.fragments.base.BaseFragment
import com.immon.truckorbit.utils.setTitle

@Suppress("DEPRECATION")
class TrucksFragment : BaseFragment() {

    private lateinit var binding: FragmentTrucksBinding

    override val isLightStatusbar: Boolean
        get() = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrucksBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        binding.header.toolbar.setTitle(requireContext(), "Trucks", false)

        binding.btnAddTruck.setOnClickListener {
            binding.btnAddTruck.startAnimation {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.addTruckContainer.visibility = View.GONE

                    binding.etTruckCompany.text.clear()
                    binding.etLicensePlate.text.clear()

                    binding.btnAddTruck.revertAnimation()
                }, 2000)
            }
        }

        binding.btnSearchTruck.setOnClickListener {
            binding.btnSearchTruck.startAnimation {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.searchTruckContainer.visibility = View.GONE

                    binding.etSearchQuery.text.clear()

                    binding.btnSearchTruck.revertAnimation()
                }, 2000)
            }
        }

        binding.btnResetSearch.setOnClickListener {
            binding.recyclerView.visibility = View.VISIBLE
            binding.searchTruckContainer.visibility = View.GONE

            binding.etSearchQuery.text.clear()
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_corner_menu_admin_trucks, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith(
            "super.onOptionsItemSelected(item)",
            "androidx.fragment.app.Fragment"
        )
    )
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                binding.addTruckContainer.visibility =
                    if (binding.addTruckContainer.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                binding.searchTruckContainer.visibility = View.GONE
            }

            R.id.search -> {
                binding.searchTruckContainer.visibility =
                    if (binding.searchTruckContainer.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                binding.addTruckContainer.visibility = View.GONE
            }
        }

        return super.onOptionsItemSelected(item)
    }
}