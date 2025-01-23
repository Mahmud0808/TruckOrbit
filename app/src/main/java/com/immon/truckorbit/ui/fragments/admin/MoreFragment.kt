package com.immon.truckorbit.ui.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentMoreBinding
import com.immon.truckorbit.utils.applyWindowInsets
import com.immon.truckorbit.utils.setLightStatusBar

class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.setLightStatusBar(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMoreBinding.inflate(inflater, container, false)

        binding.profileLayout.applyWindowInsets(top = true, bottom = false)

        return binding.root
    }
}