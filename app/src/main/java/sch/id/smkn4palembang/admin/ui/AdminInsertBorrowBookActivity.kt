package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View.OnFocusChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityAdminInsertBorrowBookBinding
import sch.id.smkn4palembang.model.BookBorrowing
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.utils.SuccessDialog
import java.text.SimpleDateFormat
import java.util.Locale

class AdminInsertBorrowBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminInsertBorrowBookBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var successDialog: SuccessDialog
    private val firestore = Firebase.firestore

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.apply {
                if (!TextUtils.isEmpty(nameEdittext.text)) {
                    nameTextInputLayout.error = null
                } else {
                    nameTextInputLayout.error = getString(R.string.is_not_empty)
                }

                if (!TextUtils.isEmpty(idEdittext.text)) {
                    idTextInputLayout.error = null
                } else {
                    idTextInputLayout.error = getString(R.string.is_not_empty)
                }

                if (!TextUtils.isEmpty(titleBookEdittext.text)) {
                    titleBookTextInputLayout.error = null
                } else {
                    titleBookTextInputLayout.error = getString(R.string.is_not_empty)
                }

                if (!TextUtils.isEmpty(isbnBookEdittext.text)) {
                    isbnBookTextInputLayout.error = null
                } else {
                    isbnBookTextInputLayout.error = getString(R.string.is_not_empty)
                }
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            binding.apply {
                val name = nameEdittext.text.toString().trim()
                val id = idEdittext.text.toString().trim()
                val title = titleBookEdittext.text.toString().trim()
                val isbn = isbnBookEdittext.text.toString().trim()
                val borrowDate = borrowDateEdittext.text.toString().trim()
                val dateReturn = dateReturnEdittext.text.toString().trim()

                saveBorrowingBookButton.isEnabled =
                    name.isNotEmpty() && id.isNotEmpty() && title.isNotEmpty() && isbn.isNotEmpty() &&
                            borrowDate.isNotEmpty() && dateReturn.isNotEmpty()

                /**
                 * menghitung berapa lama meminjam buku
                 */
                if (borrowDate.isNotEmpty() && dateReturn.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault())
                    val borrowDate = dateFormat.parse(borrowDate)
                    val returnDate = dateFormat.parse(dateReturn)

                    val diffInMillis = returnDate.time - borrowDate.time
                    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000) // Konversi ke hari

                    // Tampilkan hasilnya di TextView
                    longBorrowOfDayTextview.text = "lama meminjam: $diffInDays hari"
                }
            }
        }
    }

    private val onFocusListener = OnFocusChangeListener { view, focus ->
        binding.apply {
            if (nameEdittext.hasFocus()) {
                if (TextUtils.isEmpty(nameEdittext.text))
                    nameTextInputLayout.error = getString(R.string.is_not_empty)
            }

            if (idEdittext.hasFocus()) {
                if (TextUtils.isEmpty(idEdittext.text))
                    idTextInputLayout.error = getString(R.string.is_not_empty)
            }

            if (titleBookEdittext.hasFocus()) {
                if (TextUtils.isEmpty(titleBookEdittext.text))
                    titleBookTextInputLayout.error = getString(R.string.is_not_empty)
            }

            if (isbnBookEdittext.hasFocus()) {
                if (TextUtils.isEmpty(isbnBookEdittext.text))
                    isbnBookTextInputLayout.error = getString(R.string.is_not_empty)
            }

        }
    }

    /**
     * mengambil hasil pemindaian barcode
     */
    private val activityResultLauncherBarcode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultScan: IntentResult? =
                IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (resultScan != null) {
                if (resultScan.contents == null) {
                    // Pemindaian dibatalkan
                    Toast.makeText(this, "pemindaian dibatalkan", Toast.LENGTH_LONG).show()
                } else {
                    // Hasil pemindaian QR code
                    val scannedText = resultScan.contents
                    binding.isbnBookEdittext.setText(scannedText)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInsertBorrowBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        successDialog = SuccessDialog(this)

        binding.apply {
            /**
             * menampilkan date picker di edittext tanggal peminjaman buku
             */
            borrowDateEdittext.setOnClickListener {
                showMaterialDatePicker("Tanggal Peminjaman Buku") { timeMilliseconds ->
                    val selectedDate =
                        SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault()).format(
                            timeMilliseconds
                        )
                    borrowDateEdittext.setText(selectedDate)
                }
            }

            /**
             * menampilkan date picker di edittext tanggal pengembalian buku
             */
            dateReturnEdittext.setOnClickListener {
                showMaterialDatePicker("Tanggal Pengembalian Buku") { timeMilliseconds ->
                    val selectedDate =
                        SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault()).format(
                            timeMilliseconds
                        )
                    dateReturnEdittext.setText(selectedDate)
                }
            }

            nameEdittext.addTextChangedListener(textWatcher)
            idEdittext.addTextChangedListener(textWatcher)
            titleBookEdittext.addTextChangedListener(textWatcher)
            isbnBookEdittext.addTextChangedListener(textWatcher)
            borrowDateEdittext.addTextChangedListener(textWatcher)
            dateReturnEdittext.addTextChangedListener(textWatcher)

            nameEdittext.onFocusChangeListener = onFocusListener
            idEdittext.onFocusChangeListener = onFocusListener
            titleBookEdittext.onFocusChangeListener = onFocusListener
            isbnBookEdittext.onFocusChangeListener = onFocusListener

            appbar.setNavigationOnClickListener { finish() }

            scanIsbnButton.setOnClickListener {
                // Inisialisasi IntentIntegrator
                val integrator = IntentIntegrator(this@AdminInsertBorrowBookActivity)
                integrator.setPrompt("Pindai BARCODE ISBN Buku") // Pesan yang ditampilkan saat memindai
                integrator.setBeepEnabled(true) // Aktifkan suara
                integrator.captureActivity =
                    CaptureActivity::class.java // Atur orientasi pemindaian
                integrator.setCameraId(0)
                integrator.setBarcodeImageEnabled(true)

                activityResultLauncherBarcode.launch(integrator.createScanIntent())
            }

            saveBorrowingBookButton.setOnClickListener { insertToDatabase() }


        }
    }

    /**
     * fungsi ini digunakan untuk membuat dan menampilkan date picker
     */
    private fun showMaterialDatePicker(title: String, callback: (Long) -> Unit) {
        // untuk menampilkan date picker
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .build()

        datePicker.show(
            supportFragmentManager,
            AdminInsertBorrowBookActivity::class.java.simpleName
        )
        datePicker.addOnPositiveButtonClickListener { timeMilliseconds ->
            callback(timeMilliseconds)
        }
    }

    private fun insertToDatabase() {
        progressDialog.showProgressDialog()
        binding.apply {
            val name = nameEdittext.text.toString().trim()
            val id = idEdittext.text.toString().trim()
            val contact = contactEdittext.text.toString().trim()
            val title = titleBookEdittext.text.toString().trim()
            val isbn = isbnBookEdittext.text.toString().trim()
            val borrowDate = borrowDateEdittext.text.toString().trim()
            val returnDate = dateReturnEdittext.text.toString().trim()
            var condition = ""

            val checkedRadioButtonId = coditionBookRadiogroup.checkedRadioButtonId
            if (checkedRadioButtonId != -1) {
                when (checkedRadioButtonId) {
                    R.id.very_good_radiobutton -> condition = veryGoodRadiobutton.text.toString()
                    R.id.normal_radiobutton -> condition = normalRadiobutton.text.toString()
                    R.id.damaged_radiobutton -> condition = damagedRadiobutton.text.toString()
                }
            }

            val borrowingBook = BookBorrowing(
                name = name,
                id = id,
                contact = contact,
                title = title,
                isbn = isbn,
                condition = condition,
                borrowingDate = borrowDate,
                returnDate = returnDate,
                dateTime = Timestamp.now()
            )

            val collection = firestore.collection(Reference.BORROWING_COLLECTION)
            collection
                .add(borrowingBook)
                .addOnSuccessListener {
                    progressDialog.dismissProgressDialog()

                    successDialog.showSuccessDialog()
                    successDialog.setOnCloseListener {
                        finish()
                    }
                }
                .addOnFailureListener {
                    progressDialog.dismissProgressDialog()
                    Toast.makeText(
                        this@AdminInsertBorrowBookActivity,
                        "gagal menambah data, terjadi masalah dengan koneksi internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


}