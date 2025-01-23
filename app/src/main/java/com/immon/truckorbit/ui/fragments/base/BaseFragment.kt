package com.immon.truckorbit.ui.fragments.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.immon.truckorbit.utils.setLightStatusBar

abstract class BaseFragment : Fragment() {

    abstract val isLightStatusbar: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().window.setLightStatusBar(isLightStatusbar)
    }
}