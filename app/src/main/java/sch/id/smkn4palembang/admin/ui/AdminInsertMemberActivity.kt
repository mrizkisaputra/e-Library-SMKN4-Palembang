package sch.id.smkn4palembang.admin.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityAdminInsertMemberBinding
import sch.id.smkn4palembang.utils.FailedDialog
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.utils.SuccessDialog
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class AdminInsertMemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInsertMemberBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var successDialog: SuccessDialog
    private lateinit var failedDialog: FailedDialog

    private val firebaseStorage = Firebase.storage
    private val firestore = Firebase.firestore

    companion object {
        private val TAG = AdminInsertMemberActivity::class.java.simpleName
        private const val CAMERA_PERMISSION_CODE = 101
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(s: Editable?) {
            binding.apply {
                val name = memberNameEdittext.text.toString().trim()
                val id = idMemberEdittext.text.toString().trim()

                saveMemberButton.isEnabled =
                    !TextUtils.isEmpty(name) && !TextUtils.isEmpty(id) && id.length >= 8

                passwordMemberEdittext.setText(id)
            }
        }
    }

    /**
     * melakukan pengecekan text field tidak boleh kosong
     */
    private val onFocusListener = View.OnFocusChangeListener { view, focus ->
        binding.apply {
            val name = memberNameEdittext.text.toString().trim()
            val id = idMemberEdittext.text.toString().trim()

            if (!focus) {
                if (name.isEmpty())
                    memberNameTextInputLayout.error = getString(R.string.is_not_empty)
                else
                    memberNameTextInputLayout.error = null

                if (id.isEmpty()) {
                    idMemberTextInputLayout.error = getString(R.string.is_not_empty)
                } else {
                    idMemberTextInputLayout.error = null
                    if (id.length <= 8) {
                        idMemberTextInputLayout.error = "panjang id minimal 8"
                    }
                }
            }
        }
    }

    /**
     * digunakan untuk menghilangkan fokus pada text field
     */
    private val onEditorActionListener = OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            binding.idMemberEdittext.clearFocus()

            // menghilangkan keyboard
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.idMemberEdittext.windowToken, 0)
            true
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInsertMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        successDialog = SuccessDialog(this)
        failedDialog = FailedDialog(this)

        binding.apply {
            topAppbar.setNavigationOnClickListener { finish() }

            memberNameEdittext.addTextChangedListener(textWatcher)
            idMemberEdittext.addTextChangedListener(textWatcher)

            memberNameEdittext.onFocusChangeListener = onFocusListener
            idMemberEdittext.onFocusChangeListener = onFocusListener
            idMemberEdittext.setOnEditorActionListener(onEditorActionListener)

            takeImageButton.setOnClickListener { takePicture() }
            saveMemberButton.setOnClickListener { uploadDataMembers() }
        }
    }

    private fun uploadDataMembers() {
        val memberName = binding.memberNameEdittext.text.toString().trim()
        val memberContact = binding.contactMemberEdittext.text.toString().trim()
        val memberId = binding.idMemberEdittext.text.toString().trim()
        val memberPassword = binding.passwordMemberEdittext.text.toString().trim()

        isMemberAlreadyInDatabase(memberId) { isAlreadyInDatabase ->
            if (isAlreadyInDatabase) {
                /**
                 * jika data dengan UD sudah terdaftar
                 */
                val message = getString(R.string.id_is_registered, memberId)
                failedDialog.showFailedDialog(message)
                failedDialog.setOnCloseListener {
                    binding.memberPictureImageview.setImageDrawable(null)
                    binding.memberNameEdittext.text?.clear()
                    binding.contactMemberEdittext.text?.clear()
                    binding.idMemberEdittext.text?.clear()
                }
            } else {
                /**
                 * jika data dengan ID belum terdaftar
                 */
                if (binding.memberPictureImageview.drawable != null) {
                    progressDialog.showProgressDialog()
                    uploadMemberPhoto { imageUrl ->
                        if (imageUrl != null) {
                            Log.d(TAG, "upload gambar member berhasil: $imageUrl")

                            insertToDatabase(
                                photo = imageUrl, name = memberName, contact = memberContact,
                                id = memberId, password = memberPassword
                            )
                        } else {
                            Log.d(TAG, "upload gambar member gagal: $imageUrl")
                        }
                    }
                } else {
                    progressDialog.showProgressDialog()
                    insertToDatabase(
                        name = memberName, contact = memberContact,
                        id = memberId, password = memberPassword
                    )
                }
            }
        }
    }

    /**
     * @desc fungsi ini untuk mengecek apakah member dengan ID sudah terdaftar
     */
    private fun isMemberAlreadyInDatabase(id: String, callback: (Boolean) -> Unit) {
        progressDialog.showProgressDialog()
        firestore.collection(Reference.MEMBERS_COLLECTION)
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { resultDocument ->
                progressDialog.dismissProgressDialog()
                if (resultDocument.isEmpty) {
                    // Tidak ada anggota dengan ID ini, kirim callback dengan false.
                    callback(false)
                } else {
                    // Ada anggota dengan ID ini, kirim callback dengan true.
                    callback(true)
                }
            }
            .addOnFailureListener {
                progressDialog.dismissProgressDialog()
                // Penanganan jika ada kesalahan dalam melakukan kueri Firestore.
                callback(false) // Misalnya, kita anggap tidak ada anggota dengan ID ini.
            }
    }

    private fun insertToDatabase(
        photo: String = "", name: String,
        contact: String, id: String, password: String
    ) {
        val dateTime = LocalDateTime.now(ZoneId.of("Asia/Jakarta"))
            .format(DateTimeFormatter.ofPattern("d/MM/yyyy H:mm"))

        val member: Map<String, Any> = hashMapOf(
            "photo" to photo,
            "name" to name,
            "contact" to contact,
            "id" to id,
            "password" to password,
            "date_time" to dateTime
        )

        firestore.collection(Reference.MEMBERS_COLLECTION)
            .add(member)
            .addOnSuccessListener { documentReference ->
                progressDialog.dismissProgressDialog()

                /**
                 * mengirimkan callback oncloselistener saat button ok di klik
                 * untuk menghapus textfield jika berhasil menambahkan member
                 */
                successDialog.setOnCloseListener {
                    binding.apply {
                        memberPictureImageview.setImageDrawable(null)
                        memberNameEdittext.text?.clear()
                        contactMemberEdittext.text?.clear()
                        idMemberEdittext.text?.clear()
                    }
                }

                // menampilkan dialog sukses menambah data
                successDialog.showSuccessDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
                Toast.makeText(
                    this,
                    "Gagal Menambah Data",
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    private fun uploadMemberPhoto(callback: (String?) -> Unit) {
        // membuat referensi atau folder didalam bucket firebase storage
        val imagesBooksRef = firebaseStorage
            .getReference(Reference.IMAGES_ROOT_REF)
            .child(Reference.MEMBER_CHILD_REF)
            .child("IMG${UUID.randomUUID()}.jpeg")

        // logic upload photo
        binding.memberPictureImageview.isDrawingCacheEnabled = true
        binding.memberPictureImageview.buildDrawingCache()
        val bitmap = (binding.memberPictureImageview.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imagesBooksRef.putBytes(data)

        // jika upload gagal
        uploadTask.addOnFailureListener {
            Toast.makeText(
                this,
                "Gagal Upload Photo",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnSuccessListener { taskSnapshot ->
            if (taskSnapshot.metadata != null) {
                // mendapatkan url berkas file yang diunggah
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                }.addOnFailureListener { exception ->
                    callback(null)
                }
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val captureImage = result.data?.extras?.get("data") as Bitmap
                binding.memberPictureImageview.setImageBitmap(captureImage)
            }
        }

    private fun takePicture() {
        val resultPermissions =
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

        if (resultPermissions) {
            // izin kamera belum diberikan oleh pengguna
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            // izin kamera sudah diberikan
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(
                    this,
                    "izin kamera ditolak, aktifkan permission di pengaturan aplikasi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun launchCamera() {
        // Membuka kamera untuk mengambil foto
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcher.launch(intent)
    }
}