package com.immon.truckorbit.ui.fragments.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.databinding.FragmentMoreBinding
import com.immon.truckorbit.ui.activities.MainActivity
import com.immon.truckorbit.ui.fragments.LandingFragment
import com.immon.truckorbit.ui.fragments.base.BaseFragment
import com.immon.truckorbit.utils.applyWindowInsets


class MoreFragment : BaseFragment() {

    private lateinit var binding: FragmentMoreBinding

    override val isLightStatusbar: Boolean
        get() = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoreBinding.inflate(inflater, container, false)

        binding.profileLayout.applyWindowInsets(top = true, bottom = false)

        binding.logoutLayout.setOnClickListener {
            LocalDB.putBoolean("logged_in", false)

            MainActivity.replaceFragment(LandingFragment())
        }

        binding.showIdleTrucksSwitch.isChecked = LocalDB.getBoolean("show_idle_trucks", true)
        binding.showIdleTrucksSwitch.setOnCheckedChangeListener { _, isChecked ->
            LocalDB.putBoolean("show_idle_trucks", isChecked)
        }
        binding.showIdleTrucksLayout.setOnClickListener {
            binding.showIdleTrucksSwitch.performClick()
        }

        binding.showAdminsSwitch.isChecked = LocalDB.getBoolean("show_admins", false)
        binding.showAdminsSwitch.setOnCheckedChangeListener { _, isChecked ->
            LocalDB.putBoolean("show_admins", isChecked)
        }
        binding.showAdminsLayout.setOnClickListener {
            binding.showAdminsSwitch.performClick()
        }

        binding.privacyLayout.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.privacypolicies.com/generic/")))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open link", Toast.LENGTH_SHORT).show()
            }
        }

        binding.helpLayout.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.facebook.com/immon.abeer.5#")))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open link", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}