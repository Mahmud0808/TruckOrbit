package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentTrucksBinding
import com.immon.truckorbit.utils.setLightStatusBar
import com.immon.truckorbit.utils.setTitle

class TrucksFragment : Fragment() {

    private lateinit var binding: FragmentTrucksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.setLightStatusBar(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrucksBinding.inflate(inflater, container, false)

        binding.header.toolbar.setTitle(requireContext(), "Trucks", false)

        return binding.root
    }
}