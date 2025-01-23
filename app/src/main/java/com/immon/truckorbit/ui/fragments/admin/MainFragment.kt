package com.immon.truckorbit.ui.fragments.admin

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
        private var currentFragment: Fragment? = null

        private val myFragmentManager: FragmentManager
            get() = thisFragmentManager!!

        enum class SlideDirection {
            LEFT_TO_RIGHT,
            RIGHT_TO_LEFT,
            NONE
        }

        private fun isInGroup1(fragment: Fragment): Boolean {
            return fragment is MonitoringFragment
        }

        private fun isInGroup2(fragment: Fragment): Boolean {
            return fragment is TrucksFragment
        }

        private fun isInGroup3(fragment: Fragment): Boolean {
            return fragment is DriversFragment
        }

        private fun isInGroup4(fragment: Fragment): Boolean {
            return fragment is MoreFragment
        }

        private fun getSlidingDirection(
            currentFragment: Fragment?,
            newFragment: Fragment
        ): SlideDirection {
            if (currentFragment == null) {
                return SlideDirection.NONE
            }

            val direction = if (isInGroup1(currentFragment) && !isInGroup1(newFragment)) {
                SlideDirection.LEFT_TO_RIGHT
            } else if (isInGroup4(currentFragment) && !isInGroup4(newFragment)) {
                SlideDirection.RIGHT_TO_LEFT
            } else if (isInGroup2(currentFragment)) {
                if (isInGroup1(newFragment)) {
                    SlideDirection.RIGHT_TO_LEFT
                } else if (isInGroup3(newFragment) || isInGroup4(newFragment)) {
                    SlideDirection.LEFT_TO_RIGHT
                } else {
                    return SlideDirection.NONE
                }
            } else if (isInGroup3(currentFragment)) {
                if (isInGroup4(newFragment)) {
                    SlideDirection.LEFT_TO_RIGHT
                } else if (isInGroup1(newFragment) || isInGroup2(newFragment)) {
                    SlideDirection.RIGHT_TO_LEFT
                } else {
                    return SlideDirection.NONE
                }
            } else {
                return SlideDirection.NONE
            }

            return direction
        }

        fun replaceFragment(
            fragment: Fragment,
            slideDirection: SlideDirection = getSlidingDirection(currentFragment, fragment)
        ) {
            myFragmentManager.beginTransaction().apply {
                when (slideDirection) {
                    SlideDirection.LEFT_TO_RIGHT -> setCustomAnimations(
                        R.anim.slide_in_enter,
                        R.anim.slide_out_enter,
                        R.anim.slide_in_exit,
                        R.anim.slide_out_exit
                    )

                    SlideDirection.RIGHT_TO_LEFT -> setCustomAnimations(
                        R.anim.slide_in_exit,
                        R.anim.slide_out_exit,
                        R.anim.slide_in_enter,
                        R.anim.slide_out_enter
                    )

                    SlideDirection.NONE -> setCustomAnimations(
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
                currentFragment = fragment
            }
        }
    }
}