package sch.id.smkn4palembang.member.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View.OnFocusChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityMemberLoginBinding
import sch.id.smkn4palembang.utils.ProgressDialog

class MemberLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberLoginBinding
    private lateinit var progressDialog: ProgressDialog
    private val auth = Firebase.auth

    companion object {
    }

    /**
     * mengintai textfield, jika tidak kosong aktifkan button login
     */
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (!TextUtils.isEmpty(binding.idMemberTextInputEdittext.text)) {
                binding.idMemberTextInputLayout.error = null
            } else {
                binding.idMemberTextInputLayout.error = getString(R.string.is_not_empty)
            }

            if (!TextUtils.isEmpty(binding.passwordMemberTextInputEdittext.text)) {
                binding.passwordMemberTextInputLayout.error = null
            } else {
                binding.passwordMemberTextInputLayout.error = getString(R.string.is_not_empty)
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            binding.apply {
                memberLoginButton.isEnabled =
                    !TextUtils.isEmpty(idMemberTextInputEdittext.text) &&
                            !TextUtils.isEmpty(passwordMemberTextInputEdittext.text)
            }
        }

    }

    /**
     * textfield tidak boleh kodong
     */
    private val onFocusListener = OnFocusChangeListener { view, focus ->
        binding.apply {
            if (!idMemberTextInputEdittext.hasFocus()) {
                if (TextUtils.isEmpty(idMemberTextInputEdittext.text))
                    idMemberTextInputLayout.error = getString(R.string.is_not_empty)
            }

            if (!passwordMemberTextInputEdittext.hasFocus()) {
                if (TextUtils.isEmpty(passwordMemberTextInputEdittext.text))
                    passwordMemberTextInputLayout.error = getString(R.string.is_not_empty)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        binding.idMemberTextInputEdittext.addTextChangedListener(textWatcher)
        binding.passwordMemberTextInputEdittext.addTextChangedListener(textWatcher)
        binding.idMemberTextInputEdittext.onFocusChangeListener = onFocusListener
        binding.passwordMemberTextInputEdittext.onFocusChangeListener = onFocusListener

        binding.memberLoginButton.setOnClickListener { memberSignIn() }
    }

    /**
     * @desc fungsi untuk menangani anggota login
     */
    private fun memberSignIn() {
        progressDialog.showProgressDialog()

        val id = binding.idMemberTextInputEdittext.text.toString().trim()
        val password = binding.passwordMemberTextInputEdittext.text.toString().trim()

        auth.signInWithEmailAndPassword("$id@gmail.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    if (task.result.user != null) {
                        progressDialog.dismissProgressDialog()
                    } else {
                        Log.i("MemberLoginActivity", "login: GAGAL")
                    }
                } else {
                    progressDialog.dismissProgressDialog()
                    val snackbar = Snackbar.make(
                        binding.infoLoginCoordinatorLayout,
                        getString(R.string.failure_info_login_member),
                        Snackbar.LENGTH_SHORT
                    )
                    snackbar.setBackgroundTint(getColor(R.color.md_theme_light_error))
                    snackbar.show()
                }
            }
    }


}