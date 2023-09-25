package sch.id.smkn4palembang.admin.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AlertDialogLayout
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.adapter.AdminManagementBookBorrowingAdapter
import sch.id.smkn4palembang.admin.ui.BorrowingHistoryActivity
import sch.id.smkn4palembang.databinding.FragmentBookBorrowingBinding
import sch.id.smkn4palembang.model.BookBorrowing
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookBorrowingFragment : Fragment() {

    private var _binding: FragmentBookBorrowingBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: AdminManagementBookBorrowingAdapter
    private val listBookBorrowing = ArrayList<BookBorrowing>()

    private val firestore = Firebase.firestore

    companion object {
        private val TAG = BookBorrowingFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookBorrowingBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(requireActivity())

        initRecyclerview()
        getBookBorrowing()

        adapter.setOnUpdateItemListener { bookBorrowing, position ->
            updateItem(bookBorrowing, position)
        }

        adapter.setOnDeleteItemListener { bookBorrowing, position ->
            deleteItem(bookBorrowing, position)
        }

        binding.floatingActionButton.setOnClickListener { showPopupMenu(it) }

    }

    /**
     * menampilkan popupmenu dari floating action button
     */
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view).apply {
            menuInflater.inflate(R.menu.admin_popup_menu_book_borrowing, this.menu)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search_borrowing_menu -> {
                    true
                }

                R.id.history_borrowing_menu -> {
                    startActivity(Intent(requireActivity(), BorrowingHistoryActivity::class.java))
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    /**
     * memperpanjang masa peminjaman buku
     */
    private fun updateItem(data: BookBorrowing, position: Int) {
        /**
         * memformat tanggal pengembalian buku, untuk ditampilkan saat perpanjangan peminjaman
         */
        val dateFormat = SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault())
        val timeMilliseconds = data.returnDate?.let { dateFormat.parse(it) }

        // menambahkan satu hari di timemillisecond
        val timeMillisecondsAfterPlused: Long = timeMilliseconds?.time?.plus(24 * 60 * 60 * 1000) as Long

        // menampilkan dialog memperpanjang masa peminjaman
        createExtendBookDialog(timeMillisecondsAfterPlused) { newReturnDate ->

            // menampilkan dialog konfirmasi tindakan
            confirmAction(
                getString(R.string.title_confirm_extend),
                getString(R.string.message_confirm_extend, data.title)
            ) {
                if (newReturnDate != null) {
                    progressDialog.showProgressDialog()

                    /**
                     * lakukan update masa perpanjangan peminjaman
                     */
                    val documentId = data.documentID // id dokumen untuk update data
                    val collectionRef = firestore.collection(Reference.BORROWING_COLLECTION)

                    // lakukan update
                    documentId?.let {
                        collectionRef
                            .document(documentId)
                            .update("returnDate", newReturnDate)
                            .addOnSuccessListener {
                                progressDialog.dismissProgressDialog()
                                getBookBorrowing()

                                Snackbar.make(
                                    binding.infoCoordinator,
                                    "peminjaman buku ${data.title} oleh ${data.name} berhasil di perpanjang",
                                    Snackbar.ANIMATION_MODE_SLIDE
                                )
                                    .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_primary))
                                    .show()
                            }
                            .addOnFailureListener {
                                progressDialog.dismissProgressDialog()
                                Snackbar.make(
                                    binding.infoCoordinator,
                                    "peminjaman buku ${data.title} oleh ${data.name} gagal di perpanjang, terjadi masalah koneksi internet",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_error))
                                    .show()
                            }
                    } ?: run {
                        Log.i(TAG, "updateItem: documentID null")
                    }
                } else {
                    Snackbar.make(
                        binding.infoCoordinator,
                        "gagal, tanggal tidak boleh kosong",
                        Snackbar.LENGTH_SHORT
                    )
                        .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_error))
                        .show()
                }
            }
        }


    }

    /**
     * menampilkan dialog untuk memperpanjang masa peminjaman buku
     */
    private fun createExtendBookDialog(timeMillisecondBookReturn: Long?, callback: (String?) -> Unit) {
        val view =
            LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_extend_book, null, false)
        val extendButton: Button = view.findViewById(R.id.extend_button)
        val returnDate: TextInputEditText = view.findViewById(R.id.update_return_date_edittext)

        val alert = AlertDialog.Builder(requireActivity()).apply {
            setView(view)
            setCancelable(true)
        }.create()

        var newReturnDate: String? = null
        returnDate.setOnClickListener {
            showDatePicker(timeMillisecondBookReturn) { timeMilliseconds ->
                newReturnDate =
                    SimpleDateFormat("MMMM/dd/yyyy", Locale.getDefault()).format(timeMilliseconds)
                returnDate.setText(newReturnDate)
            }
        }
        extendButton.setOnClickListener {
            callback(newReturnDate)
            alert.dismiss()
        }

        alert.show()
    }

    private fun showDatePicker(timeMillisecondBookReturn: Long?, callback: (Long) -> Unit) {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Perpanjang Masa Peminjaman")
                .setSelection(
                    if (timeMillisecondBookReturn != null)
                        timeMillisecondBookReturn
                    else
                        MaterialDatePicker.todayInUtcMilliseconds()
                )
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .build()

        datePicker.show(childFragmentManager, BookBorrowingFragment::class.java.simpleName)
        datePicker.addOnPositiveButtonClickListener { callback(it) }
    }

    /**
     * mengkonfirmasi buku yang telah dikembalikan akan dihapus dan di simpan dalam history peminjaman buku
     */
    private fun deleteItem(data: BookBorrowing, position: Int) {
        confirmAction(
            getString(R.string.title_confirm_has_been_returned),
            getString(R.string.message_confirm_has_been_returned, data.name)
        ) {
            progressDialog.showProgressDialog()

            /**
             * sebelum dihapus dari daftar pinjaman, simpan history peminjaman
             */
            val borrowHistory = data.copy(
                name = data.name,
                id = data.id,
                title = data.title,
                isbn = data.isbn,
                condition = data.condition,
                borrowingDate = data.borrowingDate,
                returnDate = data.returnDate,
                dateTime = data.dateTime,
                timestamp = Date().time,
                contact = data.contact,
                isExpanded = false,
                documentID = null
            )

            val collection = firestore.collection(Reference.BORROWING_HISTORY_COLLECTION)
            collection
                .add(borrowHistory)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireActivity(),
                        "data peminjaman disimpan di histori",
                        Toast.LENGTH_LONG
                    ).show()
                    /**
                     * setelah disimpan dalam history, hapus dari daftar pinjaman buku
                     */
                    val docId = data.documentID
                    if (docId != null) {
                        firestore.collection(Reference.BORROWING_COLLECTION)
                            .document(docId)
                            .delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    progressDialog.dismissProgressDialog()
                                    getBookBorrowing()

                                    Snackbar.make(
                                        binding.infoCoordinator,
                                        "buku dipinjam ${data.name} berhasil di hapus",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_primary))
                                        .show()
                                } else {
                                    progressDialog.dismissProgressDialog()
                                    Snackbar.make(
                                        binding.infoCoordinator,
                                        "buku dipinjam ${data.name} gagal di hapus",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_error))
                                        .show()
                                }
                            }
                            .addOnFailureListener {
                                progressDialog.dismissProgressDialog()
                                Snackbar.make(
                                    binding.infoCoordinator,
                                    "terjadi masalah koneksi internet",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_error))
                                    .show()
                            }
                    }

                    progressDialog.dismissProgressDialog()
                }
                .addOnFailureListener {
                    progressDialog.dismissProgressDialog()
                    Snackbar.make(
                        binding.infoCoordinator,
                        "terjadi masalah koneksi internet",
                        Snackbar.LENGTH_LONG
                    )
                        .setBackgroundTint(requireActivity().getColor(R.color.md_theme_light_error))
                        .show()
                }

        }
    }

    private fun initRecyclerview() {
        adapter = AdminManagementBookBorrowingAdapter(requireActivity())
        val alphaAdapter = AlphaInAnimationAdapter(adapter)
        binding.recyclerview.adapter =
            ScaleInAnimationAdapter(alphaAdapter).apply { setDuration(500) }
        binding.recyclerview.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

    /**
     * mengambil semua data peminjaman buku
     */
    private fun getBookBorrowing() {
        progressDialog.showProgressDialog()

        val collection = firestore.collection(Reference.BORROWING_COLLECTION)
        collection
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listBookBorrowing.clear()

                if (!result.isEmpty) {
                    binding.dataIsEmptyImageview.visibility = View.GONE

                    for (doc in result) {
                        val name = doc.getString("name")
                        val id = doc.getString("id")
                        val contact = doc.getString("contact")
                        val title = doc.getString("title")
                        val isbn = doc.getString("isbn")
                        val condition = doc.getString("condition")
                        val borrowingDate = doc.getString("borrowingDate")
                        val returnDate = doc.getString("returnDate")
                        val dateTime = doc.get("dateTime")

                        val bookBorrowing = BookBorrowing(
                            name = name,
                            contact = contact,
                            id = id,
                            dateTime = dateTime,
                            title = title,
                            isbn = isbn,
                            condition = condition,
                            borrowingDate = borrowingDate,
                            returnDate = returnDate,
                            documentID = doc.id
                        )

                        listBookBorrowing.add(bookBorrowing)
                    }

                    progressDialog.dismissProgressDialog()
                    adapter.setData(listBookBorrowing)
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyDataSetChanged()
                    progressDialog.dismissProgressDialog()
                    binding.dataIsEmptyImageview.visibility = View.VISIBLE
                }

                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireActivity(),
                    "terjadi masalah koneksi internet",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismissProgressDialog()
            }
    }

    private fun confirmAction(title: String, message: String, callback: () -> Unit) {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(true)
            setPositiveButton("Konfirmasi") { _, _ -> callback() }
        }.create()
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}