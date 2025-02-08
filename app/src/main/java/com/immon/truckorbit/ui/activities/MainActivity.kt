package com.immon.truckorbit.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.SupportMapFragment
import com.immon.truckorbit.R
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.databinding.ActivityMainBinding
import com.immon.truckorbit.ui.fragments.LandingFragment
import com.immon.truckorbit.ui.fragments.admin.MainFragment
import com.immon.truckorbit.ui.fragments.driver.HomeFragment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition { isLoading }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)

        initializeDummyMap()
        setupEdgeToEdge();
        setContentView(binding.getRoot())

        thisFragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            if (LocalDB.getBoolean("logged_in", false)) { // logged in
                if (LocalDB.getBoolean("is_admin", false)) { // admin
                    replaceFragment(MainFragment())
                } else { // driver
                    replaceFragment(HomeFragment())
                }
            } else { // not logged in
                replaceFragment(LandingFragment())
            }
        }
    }

    private fun setupEdgeToEdge() {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
    }

    private fun initializeDummyMap() {
        Executors.newSingleThreadExecutor().execute {
            val latch = CountDownLatch(1)
            runOnUiThread {
                SupportMapFragment().apply {
                    supportFragmentManager
                        .beginTransaction()
                        .add(this, "dummyMapFragment")
                        .commit()

                    getMapAsync {
                        Log.d(MainActivity::class.java.simpleName, "Dummy map is ready")
                        latch.countDown()
                    }
                }
            }

            try {
                latch.await()
            } catch (ignored: InterruptedException) {
            }

            runOnUiThread { isLoading = false }
        }
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
                    is MainFragment, is HomeFragment -> {
                        myFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
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