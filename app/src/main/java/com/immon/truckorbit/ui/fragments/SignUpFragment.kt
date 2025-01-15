package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentSignUpBinding
import com.immon.truckorbit.ui.activities.MainActivity.Companion.myFragmentManager

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.btnSignUp.setOnClickListener {
            binding.btnSignUp.startAnimation {
                Handler(Looper.getMainLooper()).postDelayed({
                    myFragmentManager.popBackStack()
                }, 2000)
            }
        }

        binding.txtSignIn.setOnClickListener {
            myFragmentManager.popBackStack()
        }

        return binding.root
    }
}