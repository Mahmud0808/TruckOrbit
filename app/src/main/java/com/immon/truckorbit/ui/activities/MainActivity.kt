package com.immon.truckorbit.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable
import com.immon.truckorbit.R
import com.immon.truckorbit.data.LocalDB
import com.immon.truckorbit.databinding.ActivityMainBinding
import com.immon.truckorbit.ui.fragments.admin.MainFragment
import com.immon.truckorbit.ui.fragments.LandingFragment
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
        setContentView(binding.getRoot())
        setupEdgeToEdge()

        thisFragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            if (LocalDB.getBoolean("logged_in", false)) {
                replaceFragment(MainFragment())
            } else {
                replaceFragment(LandingFragment())
            }
        }
    }

    private fun setupEdgeToEdge() {
        try {
            val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
            appBarLayout.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(
                applicationContext
            )
        } catch (ignored: Exception) {
        }

        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
                    is MainFragment -> {
                        myFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    }

                    !is LandingFragment -> {
                        addToBackStack("OnboardFragment")
                    }
                }

                commit()
            }
        }
    }
}