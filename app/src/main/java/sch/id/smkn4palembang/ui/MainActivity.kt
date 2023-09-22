package sch.id.smkn4palembang.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.admin.ui.HomeAdminActivity
import sch.id.smkn4palembang.admin.ui.LoginActivity
import sch.id.smkn4palembang.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

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
            Log.i(TAG, "Current User: ${currentUser.email} ${currentUser.displayName}")

            startActivity(Intent(this, HomeAdminActivity::class.java))
            finish()
        } else {
            Log.i(TAG, "currentUser: $currentUser")
        }
    }

    private fun goNavigate(destination: AppCompatActivity) {
        if (destination is HomeActivity)
            Intent(this, HomeActivity::class.java).apply { startActivity(this) }

        if (destination is LoginActivity)
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
    }
}