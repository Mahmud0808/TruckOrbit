package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentLandingBinding
import com.immon.truckorbit.ui.activities.MainActivity.Companion.replaceFragment
import com.immon.truckorbit.utils.setLightStatusBar

class LandingFragment : Fragment() {

    private lateinit var binding: FragmentLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.setLightStatusBar(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLandingBinding.inflate(inflater, container, false)

        binding.btnNext.setOnClickListener {
            replaceFragment(SignInFragment(), true)
        }

        return binding.root
    }
}