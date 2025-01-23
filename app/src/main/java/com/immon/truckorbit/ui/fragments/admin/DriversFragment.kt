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
import androidx.fragment.app.Fragment
import com.immon.truckorbit.R
import com.immon.truckorbit.databinding.FragmentDriversBinding
import com.immon.truckorbit.utils.setLightStatusBar
import com.immon.truckorbit.utils.setTitle

@Suppress("DEPRECATION")
class DriversFragment : Fragment() {

    private lateinit var binding: FragmentDriversBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.setLightStatusBar(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriversBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        binding.header.toolbar.setTitle(requireContext(), "Drivers", false)

        binding.btnSearchDriver.setOnClickListener {
            binding.btnSearchDriver.startAnimation {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.searchDriverContainer.visibility = View.GONE

                    binding.etSearchQuery.text.clear()

                    binding.btnSearchDriver.revertAnimation()
                }, 2000)
            }
        }

        binding.btnResetSearch.setOnClickListener {
            binding.recyclerView.visibility = View.VISIBLE
            binding.searchDriverContainer.visibility = View.GONE

            binding.etSearchQuery.text.clear()
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_corner_menu_admin_drivers, menu)
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
            R.id.search -> {
                binding.searchDriverContainer.visibility =
                    if (binding.searchDriverContainer.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}