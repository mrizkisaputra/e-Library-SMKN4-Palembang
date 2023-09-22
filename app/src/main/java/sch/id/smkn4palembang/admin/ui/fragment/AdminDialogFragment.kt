package sch.id.smkn4palembang.admin.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.FragmentAdminDialogBinding
import sch.id.smkn4palembang.ui.MainActivity
import sch.id.smkn4palembang.utils.ProgressDialog

class AdminDialogFragment : DialogFragment() {
    private var _binding: FragmentAdminDialogBinding? = null
    private val binding get() = _binding!!
    private val auth = Firebase.auth
    private lateinit var progressDialog: ProgressDialog

    companion object {
        private val TAG = AdminDialogFragment::class.java.simpleName
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (TextUtils.isEmpty(binding.newPasswordEdittext.text)) {
                binding.newPasswordTextinputlayout.error = getString(R.string.is_not_empty)
            } else {
                binding.newPasswordTextinputlayout.error = null
                if (binding.newPasswordEdittext.text.toString().length < 6) {
                    binding.newPasswordTextinputlayout.error = "password minimal 6 karakter"
                }
            }

            if (TextUtils.isEmpty(binding.confirmPasswordEdittext.text)) {
                binding.confirmPasswordTextinputlayout.error = getString(R.string.is_not_empty)
            } else {
                binding.confirmPasswordTextinputlayout.error = null
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            binding.apply {
                val newPassword = newPasswordEdittext.text.toString().trim()
                val confirmPassword = confirmPasswordEdittext.text.toString().trim()

                updatePasswordButton.isEnabled =
                    !TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(confirmPassword) &&
                            confirmPassword.length >= 6 && newPassword.length >= 6

                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (newPassword != confirmPassword) {
                        confirmPasswordTextinputlayout.error = "konfirmasi password salah"
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminDialogBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(requireActivity())

        binding.apply {
            newPasswordEdittext.addTextChangedListener(textWatcher)
            confirmPasswordEdittext.addTextChangedListener(textWatcher)

            updatePasswordButton.setOnClickListener { updatePassword() }

        }
    }

    private fun updatePassword() {
        val newPassword = binding.newPasswordEdittext.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEdittext.text.toString().trim()

        val confirmPasswordResult = checkConfirmPassword(newPassword, confirmPassword)

        if (confirmPasswordResult) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                progressDialog.showProgressDialog()
                Log.i(TAG, "current user: ${currentUser.email}")
                currentUser
                    .updatePassword(confirmPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressDialog.dismissProgressDialog()
                            showDialogSuccess(confirmPassword)
                            Log.i(TAG, "updatePassword berhasil")
                        } else {
                            progressDialog.dismissProgressDialog()
                            Toast.makeText(
                                requireActivity(),
                                "ubah password gagal, terjadi sesuatu masalah",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.i(TAG, "updatePassword gagal: ${task.exception?.message}")
                        }
                    }
            } else {
                Log.i(TAG, "updatePassword current user: $currentUser")
            }
        } else {
            Toast.makeText(requireActivity(), "konfirmasi password salah", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkConfirmPassword(newPassword: String, confirmPassword: String): Boolean {
        return newPassword == confirmPassword
    }

    private fun showDialogSuccess(password: String) {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle("Ubah Password Berhasil")
            setMessage("password baru: $password")
            setCancelable(false)
            setPositiveButton("Login Kembali") { dialog, _ ->
                auth.signOut()
                dialog.dismiss()
                Intent(requireActivity(), MainActivity::class.java).apply { startActivity(this) }
                requireActivity().finish()
            }
        }.create()

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}