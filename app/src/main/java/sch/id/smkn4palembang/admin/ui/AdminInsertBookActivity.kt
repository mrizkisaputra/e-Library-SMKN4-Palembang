package sch.id.smkn4palembang.admin.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.checkerframework.checker.units.qual.s
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.utils.SuccessDialog
import sch.id.smkn4palembang.databinding.ActivityAdminInsertBookBinding
import sch.id.smkn4palembang.utils.Reference.BOOKS_COLLECTION
import java.io.ByteArrayOutputStream
import java.util.UUID

class AdminInsertBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInsertBookBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var successDialog: SuccessDialog
    private lateinit var captureImage: Bitmap

    /**
     * instance bucket firebase storage untuk mengakses data
     */
    private val firebaseStorage = Firebase.storage

    /**
     * instance bucket firestore database untuk mengelola data
     */
    private val firestore = Firebase.firestore

    // mengembalikan hasil tangkapan kamera
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                captureImage = result.data?.extras?.get("data") as Bitmap
                binding.bookCoverImageview.setImageBitmap(captureImage)
            }

        }

    /**
     * @desc kode ini untuk mengintai text field yang di inputkan user
     */
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            binding.apply {
                val titleBook = titleBookEdittext.text.toString().trim()
                val isbnBook = isbnBookEdittext.text.toString().trim()
                val categoryBook = categoryBook.text.toString().trim()
                val stockBook = stokBookEdittext.text.toString().trim()

                // jika semua text field tidak kosong maka tomobol di aktifkan jika kosong dimatikan
                saveBookButton.isEnabled = !TextUtils.isEmpty(titleBook) && !TextUtils.isEmpty(isbnBook) &&
                        !TextUtils.isEmpty(categoryBook) && !TextUtils.isEmpty(stockBook)

                if (stockBook.isNotEmpty()) {
                    if (stockBook.toInt() >= 1) {
                        availabilityBook.setText("tersedia")
                    } else {
                        availabilityBook.setText("tidak tersedia")
                    }
                } else {
                    availabilityBook.text = null
                }
            }
        }
    }


    /**
     * kode ini untuk menampilkan bahwa field tidak boleh kosong
     */
    private val onFocusListener = View.OnFocusChangeListener { view, focus ->
        binding.apply {
            val titleBook = titleBookEdittext.text.toString().trim()
            val isbnBook = isbnBookEdittext.text.toString().trim()
            val categoryBook = categoryBook.text.toString().trim()
            val stockBook = stokBookEdittext.text.toString().trim()

            /**
             * jika text field hilang focus dan kosong tampilkan pesan error
             * jika hilang fokus text field tidak kosong hilangkan error
             */
            if (!focus) {
                if (TextUtils.isEmpty(titleBook))
                    titleBookTextInputLayout.error = getString(R.string.is_not_empty)
                else
                    titleBookTextInputLayout.error = null

                if (TextUtils.isEmpty(isbnBook))
                    isbnBookTextInputLayout.error = getString(R.string.is_not_empty)
                else
                    isbnBookTextInputLayout.error = null

                if (TextUtils.isEmpty(categoryBook))
                    categoryBookTextInputLayout.error = getString(R.string.is_not_empty)
                else
                    categoryBookTextInputLayout.error = null

                if (TextUtils.isEmpty(stockBook))
                    stokBookTextInputLayout.error = getString(R.string.is_not_empty)
                else
                    stokBookTextInputLayout.error = null
            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInsertBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        successDialog = SuccessDialog(this)

        with(binding) {
            topAppbar.setNavigationOnClickListener { finish() }
            takeImageButton.setOnClickListener { takePicture() }
            scanIsbnButton.setOnClickListener {  }

            titleBookEdittext.addTextChangedListener(textWatcher)
            isbnBookEdittext.addTextChangedListener(textWatcher)
            categoryBook.addTextChangedListener(textWatcher)
            stokBookEdittext.addTextChangedListener(textWatcher)

            titleBookEdittext.onFocusChangeListener = onFocusListener
            isbnBookEdittext.onFocusChangeListener = onFocusListener
            categoryBook.onFocusChangeListener = onFocusListener
            stokBookEdittext.onFocusChangeListener = onFocusListener

            saveBookButton.setOnClickListener { uploadDataBook() }
        }
    }

    /**
     * @desc fungsi ini untuk mengupload data buku kedalam firestore database serta
     * upload photo buku kedalam firebase storage
     */
    private fun uploadDataBook() {
        val bookTitle = binding.titleBookEdittext.text.toString().trim()
        val bookPublisher = binding.bookPublisherEdittext.text.toString().trim()
        val numberISBN = binding.isbnBookEdittext.text.toString().trim()
        val bookLanguage = binding.bookLanguageEdittext.text.toString().trim()
        val bookCategory = binding.categoryBook.text.toString()
        val bookStok = binding.stokBookEdittext.text.toString().trim()
        val bookDescription = binding.descriptionBookEdittext.text.toString().trim()

        // untuk memastikan bahwa yang diinput sesui dengan stok
        var bookAvailability: String = if (bookStok.toInt() >= 1) {
            "tersedia"
        } else {
            "tidak tersedia"
        }

        if (binding.bookCoverImageview.drawable != null) {
            progressDialog.showProgressDialog()

            // menambahkan berkas gambar sampul buku ke firebase storage
            uploadImageCoverBook { imageUrl ->
                if (imageUrl != null) {
                    // lakukan disini untuk menambahkan data buku serta gambar sampul buku di database
                    insertToDatabase(
                        imageUrl = imageUrl, bookTitle = bookTitle, bookPublisher = bookPublisher,
                        numberISBN = numberISBN, bookLanguage = bookLanguage, bookCategory = bookCategory,
                        bookStock = bookStok, bookAvailability = bookAvailability, bookDescription = bookDescription
                    )
                } else {
                    // kalau null
                }
            }
        } else {
            progressDialog.showProgressDialog()

            // lakukan disini hanya untuk menambahkan data buku didatabase
            insertToDatabase(
                bookTitle = bookTitle, bookPublisher = bookPublisher,
                numberISBN = numberISBN, bookLanguage = bookLanguage, bookCategory = bookCategory,
                bookStock = bookStok, bookAvailability = bookAvailability, bookDescription = bookDescription
            )
        }

    }

    /**
     * @desc fungsi ini untuk menambahkan photo di firebase storage kemudian
     * di callback untuk mendapatkan string url photo dan menambahkannya kedalam firestore database
     */
    private fun uploadImageCoverBook(callback: (String?) -> Unit) {
        // membuat referensi atau folder didalam bucket firebase storage
        val imagesBooksRef = firebaseStorage
            .getReference(Reference.IMAGES_ROOT_REF)
            .child(Reference.BOOKS_CHILD_REF)
            .child(Reference.FILE_NAME_REF)

        // logic upload photo
        binding.bookCoverImageview.isDrawingCacheEnabled = true
        binding.bookCoverImageview.buildDrawingCache()
        val bitmap = (binding.bookCoverImageview.drawable as BitmapDrawable).bitmap
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
                }.addOnFailureListener {exception ->
                    callback(null)
                    Log.e(TAG, "Gagal mendapatkan URL Photo: ${exception.message}")
                }
            }
        }
    }

    /**
     * @desc fungsi ini untuk menambahkan data buku kedalam firestore database
     */
    private fun insertToDatabase(
        imageUrl: String = "", bookTitle: String, bookPublisher: String,
        numberISBN: String, bookLanguage: String, bookCategory: String,
        bookStock: String, bookAvailability: String, bookDescription: String
    ) {
        val book: Map<String, Any> = hashMapOf(
            "cover" to imageUrl,
            "title" to bookTitle,
            "publisher" to bookPublisher,
            "isbn" to numberISBN,
            "language" to bookLanguage,
            "category" to bookCategory,
            "stok" to bookStock,
            "availability" to bookAvailability,
            "description" to bookDescription,
            "timestamp" to Timestamp.now()
        )

        firestore.collection(Reference.BOOKS_COLLECTION)
            .add(book)
            .addOnSuccessListener { documentReference ->
                progressDialog.dismissProgressDialog()

                /**
                 * mengirimkan callback oncloselistener saat button ok di klik
                 * untuk menghapus textfield jika berhasil menambahkan buku
                 */
                successDialog.setOnCloseListener {
                    binding.apply {
                        bookCoverImageview.setImageDrawable(null)
                        titleBookEdittext.text?.clear()
                        bookPublisherEdittext.text?.clear()
                        isbnBookEdittext.text?.clear()
                        bookLanguageEdittext.text?.clear()
                        categoryBook.text?.clear()
                        stokBookEdittext.text?.clear()
                        availabilityBook.text?.clear()
                        descriptionBookEdittext.text?.clear()
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

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncher.launch(intent)
    }

    companion object {
        private val TAG: String = AdminInsertBookActivity::class.java.simpleName

    }

}