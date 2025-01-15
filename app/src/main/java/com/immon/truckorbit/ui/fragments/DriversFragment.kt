package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentDriversBinding
import com.immon.truckorbit.utils.setLightStatusBar
import com.immon.truckorbit.utils.setTitle

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

        binding.header.toolbar.setTitle(requireContext(), "Drivers", false)

        return binding.root
    }
}