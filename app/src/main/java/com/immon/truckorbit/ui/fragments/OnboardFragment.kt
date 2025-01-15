package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentOnboardBinding
import com.immon.truckorbit.ui.activities.MainActivity.Companion.replaceFragment
import com.immon.truckorbit.utils.setLightStatusBar

class OnboardFragment : Fragment() {

    private lateinit var binding: FragmentOnboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.setLightStatusBar(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardBinding.inflate(inflater, container, false)

        binding.btnNext.setOnClickListener {
            replaceFragment(SignInFragment(), true)
        }

        return binding.root
    }
}