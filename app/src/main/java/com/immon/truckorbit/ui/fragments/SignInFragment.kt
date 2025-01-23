package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.immon.truckorbit.databinding.FragmentSignInBinding
import com.immon.truckorbit.ui.activities.MainActivity.Companion.replaceFragment
import com.immon.truckorbit.ui.fragments.admin.MainFragment

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.btnSignIn.setOnClickListener {
            binding.btnSignIn.startAnimation {
                Handler(Looper.getMainLooper()).postDelayed({
                    replaceFragment(MainFragment())
                }, 2000)
            }
        }

        binding.txtCreateAccount.setOnClickListener {
            replaceFragment(SignUpFragment(), true)
        }

        return binding.root
    }
}