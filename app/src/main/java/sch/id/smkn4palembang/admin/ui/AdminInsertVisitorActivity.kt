package sch.id.smkn4palembang.admin.ui

import android.R.attr.button
import android.content.Intent
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.utils.SuccessDialog
import sch.id.smkn4palembang.databinding.ActivityAdminInsertVisitorBinding
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID


class AdminInsertVisitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInsertVisitorBinding
    private lateinit var captureImage: Bitmap
    private lateinit var progressDialog: ProgressDialog
    private lateinit var successDialog: SuccessDialog

    private val firebaseStorage = Firebase.storage
    private val firestore = Firebase.firestore

    // mengembalikan hasil tangkapan kamera
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                captureImage = result.data?.extras?.get("data") as Bitmap
                binding.visitorPictureImageview.setImageBitmap(captureImage)
            }

        }

    /**
     *  untuk mengintai edittext saat user menginputkan text
     */
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            val visitorName = binding.visitorNameEdittext.text.toString().trim()
            val visitorId = binding.identitasVisitorEdittext.text.toString().trim()

            /**
             * jika kedua edittext tidak kosong kita aktifkan tombol button dan
             * jika panjang dari edittext melebihi dari 30 dan melebihi dari 14 kita matikan
             */
            binding.saveVisitorButton.isEnabled =
                !TextUtils.isEmpty(visitorName) && !TextUtils.isEmpty(visitorId) &&
                        visitorName.length >= 4 && visitorName.length <= 30 &&
                        visitorId.length <= 14

        }
    }

    /**
     * untuk menangani text input tidak boleh kosong
     */
    private val onFocusListener = View.OnFocusChangeListener { p0, focus ->
        binding.apply {
            val visitorName = visitorNameEdittext.text.toString().trim()
            val visitorId = identitasVisitorEdittext.text.toString().trim()

            // jika tidak punya focus
            if (!visitorNameEdittext.hasFocus()) {
                if (TextUtils.isEmpty(visitorName)) {
                    // jika tidak punya focus tapi text input kosong
                    visitorNameTextInputLayout.error = getString(R.string.is_not_empty)
                } else {
                    // jika tidak punya fokus tapi text input tidak kosong
                    visitorNameTextInputLayout.error = null
                    if (visitorName.length > 30) visitorNameTextInputLayout.error =
                        "melebihi batas maximum"
                }
            }

            // jika tidak punya focus
            if (!identitasVisitorEdittext.hasFocus()) {
                if (TextUtils.isEmpty(visitorName)) {
                    // jika tidak punya focus tapi text input kosong
                    identitasVisitorTextInputLayout.error = getString(R.string.is_not_empty)
                } else {
                    // jika tidak punya fokus tapi text input tidak kosong
                    identitasVisitorTextInputLayout.error = null
                    if (visitorId.length > 14) identitasVisitorTextInputLayout.error =
                        "melebihi batas maximum"
                }
            }
        }
    }

    /**
     * untuk menghilangkan focus saat tombol done keyboard di tekan
     */
    private val onEditorListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            binding.identitasVisitorEdittext.clearFocus()
            true
        } else {
            false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInsertVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        progressDialog = ProgressDialog(this)
        successDialog = SuccessDialog(this)

        with(binding) {
            topAppbar.setNavigationOnClickListener { finish() }
            takeImageButton.setOnClickListener { takePicture() }
            saveVisitorButton.setOnClickListener { uploadDataVisitor() }

            visitorNameEdittext.addTextChangedListener(textWatcher)
            identitasVisitorEdittext.addTextChangedListener(textWatcher)

            visitorNameEdittext.onFocusChangeListener = onFocusListener
            identitasVisitorEdittext.onFocusChangeListener = onFocusListener

            identitasVisitorEdittext.setOnEditorActionListener(onEditorListener)
        }
    }

    /**
     * @desc fungsi untuk mengupload data visitor ke firestore database
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadDataVisitor() {
        binding.apply {
            val visitorName = visitorNameEdittext.text.toString().trim()
            val visitorId = identitasVisitorEdittext.text.toString().trim()
            var visitorRole: String = ""

            val checkedRadioButton = roleVisitorRadiogroup.checkedRadioButtonId
            if (checkedRadioButton != -1) {
                visitorRole = when (roleVisitorRadiogroup.checkedRadioButtonId) {
                    R.id.student_visitor_radiobutton -> getString(R.string.student)
                    R.id.teacher_visitor_radiobutton -> getString(R.string.teacher)
                    else -> { "tidak ada role" }
                }
            }

            /**
             * lakukan disni untuk menambah data visitor
             */
            if (visitorPictureImageview.drawable != null) {
                progressDialog.showProgressDialog()

                // jika ada photo, tambahkan data visitor beserta photo didalam firestore database
                uploadImageVisitor { imageUrl ->
                    if (imageUrl != null) {
                        insertToDatabse(
                            imageUrl = imageUrl, visitorName = visitorName,
                            visitorId = visitorId, visitorRole = visitorRole
                        )
                    }
                }
            } else {
                progressDialog.showProgressDialog()

                // jika tidak ada photo, tambahkan data visitor didalam firestore database
                insertToDatabse(visitorName = visitorName, visitorId = visitorId, visitorRole = visitorRole)
            }

        }
    }

    /**
     * @desc fungsi untuk upload data photo di firebase storage
     * @param {callback} untuk mendapatkan string url photo yang diupload
     */
    private fun uploadImageVisitor(callback: (String?) -> Unit) {
        val imageVisitorRef = firebaseStorage
            .getReference(Reference.IMAGES_ROOT_REF)
            .child(Reference.VISITOR_CHILD_REF)
            .child(Reference.FILE_NAME_REF)

        binding.visitorPictureImageview.isDrawingCacheEnabled = true
        binding.visitorPictureImageview.buildDrawingCache()
        val bitmap = (binding.visitorPictureImageview.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageVisitorRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(
                this,
                "Gagal Upload Photo",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            if (taskSnapshot.metadata != null) {
                // mendapatkan url berkas file yang diunggah
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                    Log.i(TAG, "IMAGE URL: $imageUrl")
                }.addOnFailureListener {exception ->
                    callback(null)
                    Log.e(TAG, "Gagal mendapatkan URL Photo: ${exception.message}")
                }
            }
        }
    }

    /**
     * @desc fungsi untuk menambahkan data visitor di firestore database
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertToDatabse(imageUrl: String = "",
                                visitorName: String,
                                visitorId: String,
                                visitorRole: String) {
        val dateTime = LocalDateTime
            .now(ZoneId.of("Asia/Jakarta"))
            .format(DateTimeFormatter.ofPattern("d/MM/yyyy H:mm"))

        // menetapkan isi data dokumen
        val visitor: Map<String, Any> = hashMapOf(
            "photo" to imageUrl,
            "name" to visitorName,
            "id_card" to visitorId,
            "role" to visitorRole,
            "visiting_time" to dateTime,
            "time_stamp" to Date().time
        )

        // membuat collection dan menambahkan dokumen
        firestore.collection(VISITORS_COLLECTION)
            .add(visitor)
            .addOnSuccessListener { documentReference ->
                progressDialog.dismissProgressDialog()

                successDialog.setOnCloseListener {
                    binding.apply {
                        visitorPictureImageview.setImageDrawable(null)
                        visitorNameEdittext.text?.clear()
                        identitasVisitorEdittext.text?.clear()
                    }
                }
                successDialog.showSuccessDialog()
                Log.i(TAG, "insertToDatabse: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                progressDialog.dismissProgressDialog()
                Toast.makeText(
                    this,
                    "Gagal Menambah Data Pengunjung",
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncher.launch(intent)
    }


    companion object {
        private val TAG = AdminInsertVisitorActivity::class.java.simpleName
        private const val VISITORS_COLLECTION = "visitors"
    }
}