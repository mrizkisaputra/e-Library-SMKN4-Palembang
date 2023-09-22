package sch.id.smkn4palembang.admin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        auth = Firebase.auth

        binding.adminLoginButton.setOnClickListener { login() }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.i(TAG, "Current User Admin: ${currentUser.email} ${currentUser.displayName}")
            updateUI()
        } else {
            Log.i(TAG, "Current User Admin: $currentUser")
        }
    }

    private fun login() {
        with(binding) {
            val username = usernameAdminTextInputEdittext.text.toString().trim()
            val password = passwordAdminTextInputEdittext.text.toString().trim()

            val isValid = checkLoginInput(username, password)

            if (isValid) {
                progressDialog.showProgressDialog()
                // kode logic login
                auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            // login berhasil
                            if (task.result.user != null) {
                                progressDialog.dismissProgressDialog()
                                val user = auth.currentUser
                                updateUI()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Gagal",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i(TAG, "login: GAGAL")
                            }
                        } else {
                            // login gagal
                            progressDialog.dismissProgressDialog()
                            val snackbar = Snackbar.make(
                                binding.infoLoginCoordinatorLayout,
                                getString(R.string.failure_info_login_admin),
                                Snackbar.LENGTH_SHORT
                            )
                            snackbar.setBackgroundTint(getColor(R.color.md_theme_light_error))
                            snackbar.show()
                        }
                    }
            }
        }
    }

    private fun checkLoginInput(username: String, password: String): Boolean {
        binding.usernameAdminTextInputLayout.error = null
        binding.passwordAdminTextInputLayout.error = null

        if (username.isBlank()) {
            binding.usernameAdminTextInputLayout.error = getString(R.string.username_is_not_empty)
            return false
        }

        if (password.isBlank()) {
            binding.passwordAdminTextInputLayout.error = getString(R.string.password_is_not_empty)
            return false
        }

        return true
    }

    private fun updateUI() {
        Intent(this, HomeAdminActivity::class.java).apply { startActivity(this) }
    }

}