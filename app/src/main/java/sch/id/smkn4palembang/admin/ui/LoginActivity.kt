package sch.id.smkn4palembang.admin.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        auth = Firebase.auth
//        createAdminUser()


        with(binding) {
            adminLoginButton.setOnClickListener { createAdminUser() }
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI()
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

    private fun createAdminUser() {
        auth.fetchSignInMethodsForEmail(USERNAME_ADMIN)
            .addOnCompleteListener { task ->
                if (task.isComplete) {
                    val signInMethod = task.result.signInMethods
                    if (!signInMethod.isNullOrEmpty()) {
                        /**
                         * Email yang telah ditentukan belum terdaftar, jalankan untuk membuat admin user
                         */
                        Log.i(TAG, "Admin dengan email belum terdaftar")
                        auth.createUserWithEmailAndPassword(USERNAME_ADMIN, PASSWORD_ADMIN)
                            .addOnCompleteListener { task ->
                                if (task.isComplete) {
                                    val firebaseUser = task.result.user
                                    // Sekarang, Anda dapat memperbarui profil pengguna dengan UUID kustom Anda.
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        // menetapkan UUID kustom di sini
                                        .setDisplayName("UUID_ADMIN_LOGIN")
                                        .build()

                                    firebaseUser?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { profileUpdateTask ->
                                            if (profileUpdateTask.isSuccessful) {
                                                Log.d("TESTING", "Profil pengguna diperbarui dengan UUID kustom")
                                            } else {
                                                Log.w("TESTING", "Gagal memperbarui profil pengguna dengan UUID kustom", profileUpdateTask.exception)
                                            }
                                        }

                                } else {
                                    Log.w("TESTING", "create admin user:failure", task.exception)
                                }
                            }
                    } else {
                        // Email sudah terdaftar, tindakan sesuai kebutuhan Anda
                        login()
                    }
                } else {
                    // Error saat memeriksa email, tindakan sesuai kebutuhan Anda
                }
            }
    }


    companion object {
        private val TAG = HomeAdminActivity::class.java.simpleName
        const val USERNAME_ADMIN = "admin@gmail.com"
        const val PASSWORD_ADMIN = "rahasiaAdmin"
    }

}