package sch.id.smkn4palembang.admin.ui

import android.Manifest
import android.R.attr.data
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityAdminInsertBookBinding
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.utils.Reference.BOOKS_COLLECTION
import sch.id.smkn4palembang.utils.SuccessDialog
import java.io.ByteArrayOutputStream
import java.util.UUID


class AdminInsertBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInsertBookBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var successDialog: SuccessDialog
    private lateinit var captureBitmap: Bitmap

    /**
     * instance bucket firebase storage untuk mengakses data
     */
    private val firebaseStorage = Firebase.storage

    /**
     * instance bucket firestore database untuk mengelola data
     */
    private val firestore = Firebase.firestore

    // mengembalikan hasil tangkapan kamera
    private val activityResultLauncherTakePicture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                binding.bookCoverImageview.setImageBitmap(imageBitmap)
            }

        }

    /**
     * @desc kode ini untuk mengintai text field yang di inputkan user
     */
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.apply {
                if (!TextUtils.isEmpty(isbnBookEdittext.text))
                    isbnBookTextInputLayout.error = null
                else
                    isbnBookTextInputLayout.error = getString(R.string.is_not_empty)

                if (!TextUtils.isEmpty(titleBookEdittext.text))
                    titleBookTextInputLayout.error = null
                else
                    titleBookTextInputLayout.error = getString(R.string.is_not_empty)

                if (!TextUtils.isEmpty(categoryBook.text))
                    categoryBookTextInputLayout.error = null
                else
                    categoryBookTextInputLayout.error = getString(R.string.is_not_empty)

                if (!TextUtils.isEmpty(stokBookEdittext.text))
                    stokBookTextInputLayout.error = null
                else
                    stokBookTextInputLayout.error = getString(R.string.is_not_empty)
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            binding.apply {
                val titleBook = titleBookEdittext.text.toString().trim()
                val isbnBook = isbnBookEdittext.text.toString().trim()
                val categoryBook = categoryBook.text.toString().trim()
                val stockBook = stokBookEdittext.text.toString().trim()

                // jika semua text field tidak kosong maka tomobol di aktifkan jika kosong dimatikan
                saveBookButton.isEnabled =
                    !TextUtils.isEmpty(titleBook) && !TextUtils.isEmpty(isbnBook) &&
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

    private val activityResultLauncherBarcode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultScan: IntentResult? =
                IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (resultScan != null) {
                if (resultScan.contents == null) {
                    // Pemindaian dibatalkan
                    Toast.makeText(this, "Pemindaian dibatalkan", Toast.LENGTH_LONG).show()
                } else {
                    // Hasil pemindaian QR code
                    val scannedText = resultScan.contents
                    binding.isbnBookEdittext.setText(scannedText)
                }
            }
        }

    companion object {
        private val TAG: String = AdminInsertBookActivity::class.java.simpleName
        private const val CAMERA_PERMISSION_CODE = 101

        private const val TAKE_PICTURE_REQUEST_CODE = 10
        private const val TAKE_CROP_PICTURE_REQUEST_CODE = 20
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

            titleBookEdittext.addTextChangedListener(textWatcher)
            isbnBookEdittext.addTextChangedListener(textWatcher)
            categoryBook.addTextChangedListener(textWatcher)
            stokBookEdittext.addTextChangedListener(textWatcher)

            titleBookEdittext.onFocusChangeListener = onFocusListener
            isbnBookEdittext.onFocusChangeListener = onFocusListener
            categoryBook.onFocusChangeListener = onFocusListener
            stokBookEdittext.onFocusChangeListener = onFocusListener

            saveBookButton.setOnClickListener { uploadDataBook() }

            scanIsbnButton.setOnClickListener {
                // Inisialisasi IntentIntegrator
                val integrator = IntentIntegrator(this@AdminInsertBookActivity)
                integrator.setPrompt("Pindai BARCODE ISBN Buku") // Pesan yang ditampilkan saat memindai
                integrator.setBeepEnabled(true) // Aktifkan suara
                integrator.captureActivity =
                    CaptureActivity::class.java // Atur orientasi pemindaian
                integrator.setCameraId(0)
                integrator.setBarcodeImageEnabled(true)

                activityResultLauncherBarcode.launch(integrator.createScanIntent())
            }

            captureTextRecognizeButton.setOnClickListener {
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setActivityTitle("Pengenalan Text")
                    .start(this@AdminInsertBookActivity)
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val croppedUri = result.uri
                // Menggunakan URI hasil potongan gambar untuk melakukan text recognition
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, croppedUri)
                getTextFromImage(bitmap)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                // Handle error jika terjadi kesalahan saat memotong gambar
            }
        }
    }
    private fun getTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                binding.descriptionBookEdittext.setText(visionText.text)
            }
            .addOnFailureListener {

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
        val bookAvailability: String = if (bookStok.toInt() >= 1) {
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
                        imageUrl = imageUrl,
                        bookTitle = bookTitle,
                        bookPublisher = bookPublisher,
                        numberISBN = numberISBN,
                        bookLanguage = bookLanguage,
                        bookCategory = bookCategory,
                        bookStock = bookStok,
                        bookAvailability = bookAvailability,
                        bookDescription = bookDescription
                    )
                } else {
                    // kalau null
                }
            }
        } else {
            progressDialog.showProgressDialog()

            // lakukan disini hanya untuk menambahkan data buku didatabase
            insertToDatabase(
                bookTitle = bookTitle,
                bookPublisher = bookPublisher,
                numberISBN = numberISBN,
                bookLanguage = bookLanguage,
                bookCategory = bookCategory,
                bookStock = bookStok,
                bookAvailability = bookAvailability,
                bookDescription = bookDescription
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
            .child("IMG${UUID.randomUUID()}.jpeg")

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
                }.addOnFailureListener { exception ->
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
            "search_title" to bookTitle.lowercase(),
            "publisher" to bookPublisher,
            "isbn" to numberISBN,
            "language" to bookLanguage,
            "category" to bookCategory.lowercase(),
            "stok" to bookStock,
            "availability" to bookAvailability,
            "description" to bookDescription,
            "timestamp" to Timestamp.now()
        )

        firestore.collection(BOOKS_COLLECTION)
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
        // periksa apakah izin kamera sudah diberikan
        val permissionResult =
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (permissionResult) {
            // izin kamera sudah diberikan
            launchCamera()
        } else {
            // izin kamera belum diberikan, minta izin kepada pengguna
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // izin kamera diberikan oleh pengguna
                launchCamera()
            } else {
                // izin di tolak oleh pengguna, berikan
                Toast.makeText(
                    this,
                    "Izin Kamera Di tolak, Aktifkan Permission di Pengaturan Aplikasi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncherTakePicture.launch(intent)
    }

}