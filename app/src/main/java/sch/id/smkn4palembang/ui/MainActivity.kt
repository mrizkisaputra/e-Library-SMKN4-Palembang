package sch.id.smkn4palembang.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.admin.ui.HomeAdminActivity
import sch.id.smkn4palembang.admin.ui.LoginActivity
import sch.id.smkn4palembang.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeMenuButton.setOnClickListener { goNavigate(HomeActivity()) }
        binding.adminLoginButton.setOnClickListener { goNavigate(LoginActivity()) }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            updateUI()
        }
    }

    private fun updateUI() {
        Intent(this, HomeAdminActivity::class.java).apply { startActivity(this) }
    }

    private fun goNavigate(destination: AppCompatActivity) {
        if (destination is HomeActivity)
            Intent(this, HomeActivity::class.java).apply { startActivity(this) }

        if (destination is LoginActivity)
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
    }
}