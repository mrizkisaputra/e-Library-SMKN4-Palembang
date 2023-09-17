package sch.id.smkn4palembang.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityHomeBinding
import sch.id.smkn4palembang.ui.fragment.DashboardFragment
import sch.id.smkn4palembang.ui.fragment.InformationFragment
import sch.id.smkn4palembang.ui.fragment.LibrarianFragment


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var doubleBackToExitPressedOnce = false
    private val fragmentManager: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.home_menu
        replaceFragment(DashboardFragment())
        binding.bottomNavigationView.setOnItemSelectedListener { OnItemSelectedListener(it) }



    }

    override fun onStart() {
        super.onStart()
        binding.bottomNavigationView.selectedItemId = R.id.home_menu
    }


    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun OnItemSelectedListener(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.home_menu -> {
                replaceFragment(DashboardFragment())
                true
            }

            R.id.pustakawan_menu -> {
                replaceFragment(LibrarianFragment())
                true
            }

            R.id.information_menu -> {
                replaceFragment(InformationFragment())
                true
            }

            else -> false
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        fragmentManager.beginTransaction().apply {
            if (fragment is DashboardFragment) {
                replace(R.id.container_framelayout, fragment, DashboardFragment::class.java.simpleName)
                commit()
            }

            if (fragment is InformationFragment) {
                replace(R.id.container_framelayout, fragment, InformationFragment::class.java.simpleName)
                commit()
            }

            if (fragment is LibrarianFragment) {
                replace(R.id.container_framelayout, fragment, LibrarianFragment::class.java.simpleName)
                commit()
            }
        }
    }


}