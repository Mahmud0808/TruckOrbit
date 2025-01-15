package com.immon.truckorbit.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.immon.truckorbit.R
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalDB.putBoolean("logged_in", true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        thisFragmentManager = childFragmentManager

        if (savedInstanceState == null) {
            replaceFragment(MonitoringFragment())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigationView()
        registerOnBackPressedCallback()
    }

    private fun setupBottomNavigationView() {
        childFragmentManager.addOnBackStackChangedListener {
            val tag = getTopFragment(childFragmentManager)

            when (tag) {
                "MonitoringFragment" -> {
                    binding.bottomNavigationView.menu.getItem(0).setChecked(true)
                }

                "TrucksFragment" -> {
                    binding.bottomNavigationView.menu.getItem(1).setChecked(true)
                }

                "DriversFragment" -> {
                    binding.bottomNavigationView.menu.getItem(2).setChecked(true)
                }

                "MoreFragment" -> {
                    binding.bottomNavigationView.menu.getItem(3).setChecked(true)
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_monitoring -> {
                    replaceFragment(MonitoringFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_trucks -> {
                    replaceFragment(TrucksFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_drivers -> {
                    replaceFragment(DriversFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_more -> {
                    replaceFragment(MoreFragment())
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

        binding.bottomNavigationView.setOnItemReselectedListener { }
    }

    private fun registerOnBackPressedCallback() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragmentManager = childFragmentManager
                    if (fragmentManager.backStackEntryCount > 0) {
                        fragmentManager.popBackStack()
                    } else {
                        requireActivity().finish()
                    }
                }
            })
    }

    private fun getTopFragment(fragmentManager: FragmentManager): String {
        val fragment = arrayOf<String?>(null)
        val last = fragmentManager.fragments.size - 1

        if (last >= 0) {
            val topFragment = fragmentManager.fragments[last]

            fragment[0] = when (topFragment) {
                is MonitoringFragment -> "MonitoringFragment"
                is TrucksFragment -> "TrucksFragment"
                is DriversFragment -> "DriversFragment"
                is MoreFragment -> "MoreFragment"
                else -> ""
            }
        }

        return fragment[0] ?: ""
    }

    companion object {
        private var thisFragmentManager: FragmentManager? = null

        val myFragmentManager: FragmentManager
            get() = thisFragmentManager!!

        fun replaceFragment(fragment: Fragment, slideAnimation: Boolean = false) {
            myFragmentManager.beginTransaction().apply {
                if (slideAnimation) {
                    setCustomAnimations(
                        R.anim.slide_in_enter,
                        R.anim.slide_out_enter,
                        R.anim.slide_in_exit,
                        R.anim.slide_out_exit
                    )
                } else {
                    setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                }
                replace(R.id.fragmentContainer, fragment)

                when (fragment) {
                    is MonitoringFragment -> {
                        myFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }

                    is TrucksFragment, is DriversFragment, is MoreFragment -> {
                        myFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )

                        when (fragment) {
                            is TrucksFragment -> addToBackStack("TrucksFragment")
                            is DriversFragment -> addToBackStack("DriversFragment")
                            else -> addToBackStack("MoreFragment")
                        }
                    }

                    else -> {
                        addToBackStack(fragment.javaClass.simpleName)
                    }
                }

                commit()
            }
        }
    }
}