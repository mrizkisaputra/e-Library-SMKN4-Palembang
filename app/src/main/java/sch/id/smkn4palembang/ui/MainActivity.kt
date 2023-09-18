package sch.id.smkn4palembang.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.admin.ui.HomeAdminActivity
import sch.id.smkn4palembang.admin.ui.LoginActivity
import sch.id.smkn4palembang.databinding.ActivityMainBinding
import sch.id.smkn4palembang.member.ui.HomeMemberActivity

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
//            updateUI(currentUser)
            val isAdmin = checkIfUserIsAdmin(currentUser)

            // Berdasarkan status, arahkan ke aktivitas yang sesuai
            if (isAdmin) {
                startActivity(Intent(this, HomeAdminActivity::class.java))
            } else {
                startActivity(Intent(this, HomeMemberActivity::class.java))
            }
            // Pastikan untuk menutup aktivitas saat ini jika perlu
            finish()
        }
    }

    private fun checkIfUserIsAdmin(user: FirebaseUser): Boolean {
        // Lakukan pengecekan status pengguna di sini, misalnya berdasarkan informasi di Firebase Database atau Firestore
        // Return true jika pengguna adalah admin, false jika pengguna adalah member
        // Contoh sederhana:
        var result: Boolean = true
        Log.i("TESTING", "checkIfUserIsAdmin: ${user.uid} ${user.email} ${user.displayName}")

        if (user.displayName != null && user.displayName == "UUID_ADMIN_LOGIN") {
            result = true
        } else {
            result = false
            Log.i("TESTING", "MEMBER LOGIN")
        }

        return result
    }

    /*private fun updateUI(user: FirebaseUser) {
        Intent(this, HomeAdminActivity::class.java).apply { startActivity(this) }
        Intent(this, HomeMemberActivity::class.java).apply { startActivity(this) }

    }*/

    private fun goNavigate(destination: AppCompatActivity) {
        if (destination is HomeActivity)
            Intent(this, HomeActivity::class.java).apply { startActivity(this) }

        if (destination is LoginActivity)
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
    }
}