package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.adapter.AdminManagementBooksAdapter
import sch.id.smkn4palembang.admin.utils.AlertDialog
import sch.id.smkn4palembang.admin.utils.ProgressDialog
import sch.id.smkn4palembang.databinding.ActivityAdminManagementBooksBinding
import sch.id.smkn4palembang.model.Book

class AdminManagementBooksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManagementBooksBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertDialog: AlertDialog

    private val firestore = Firebase.firestore
    private val listBook: ArrayList<Book> = ArrayList()
    private lateinit var adminManagementBooksAdapter: AdminManagementBooksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alertDialog = AlertDialog(this)
        progressDialog = ProgressDialog(this)

        initRecyclerview()
        getBooks()
        // menangani ketika salah satu item list buku di klik
        adminManagementBooksAdapter.setOnItemClickListener(::onItemBookClick)

        binding.apply {
            searchView.editText.setOnEditorActionListener { textView, actionId, keyEvent ->
                searchView.hide()
                searchBar.text = searchView.text
                searchBooks(searchView.text.toString())
                true
            }
        }
    }

    private fun onItemBookClick(book: Book) {
        alertDialog.createAlertDialog(book)

        alertDialog.setOnButtonClickListener(
            {
                // disini tempat menangani delete item
            },
            {
                // disini tempat menangani update item
            }
        )
    }

    /**
     * @desc fungsi ini untuk menginisialisasi recyclerview
     */
    private fun initRecyclerview() {
        adminManagementBooksAdapter = AdminManagementBooksAdapter(this)
        binding.apply {
            recyclerview.adapter = ScaleInAnimationAdapter(adminManagementBooksAdapter).apply { setDuration(500) }
            recyclerview.layoutManager = GridLayoutManager(
                this@AdminManagementBooksActivity,
                2,
                GridLayoutManager.VERTICAL,
                false
            )
            recyclerview.setHasFixedSize(true)
        }
    }

    /**
     * @Desc fungsi ini untuk mengambil data dari firestore dan menampilkan data terbaru di paling atas
     */
    private fun getBooks() {
        progressDialog.showProgressDialog()

        firestore.collection(AdminInsertBookActivity.BOOKS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listBook.clear()

                // mengambil semua dokumen
                for (document in result) {
                    val cover = document.getString("cover")
                    val title = document.getString("title")
                    val isbn = document.getString("isbn")
                    val category = document.getString("category")
                    val stock = document.getString("stok")
                    val availability = document.getString("availability")

                    val book = Book(
                        cover = cover, title = title,
                        isbn = isbn, category = category,
                        stock = stock, availability = availability
                    )

                    listBook.add(book)
                }

                // Setelah mengambil dan mengurutkan data, update RecyclerView
                adminManagementBooksAdapter.setData(listBook)
                adminManagementBooksAdapter.notifyDataSetChanged()
                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    /**
     * @desc fungsi ini untuk mecari data buku berdasarkan judul, kemudian menampilkkan hasil pencarian
     */
    private fun searchBooks(keyword: String) {
        showProgressBar(true)
        firestore
            .collection(AdminInsertBookActivity.BOOKS_COLLECTION)
            .orderBy("title")
            .startAt(keyword)
            .endAt(keyword + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                listBook.clear()
                // List untuk hasil pencarian
                val searchResults = ArrayList<Book>()

                for (document in result) {
                    val cover = document.getString("cover")
                    val title = document.getString("title")
                    val isbn = document.getString("isbn")
                    val category = document.getString("category")
                    val stock = document.getString("stok")
                    val availability = document.getString("availability")

                    val book = Book(
                        cover = cover, title = title,
                        isbn = isbn, category = category,
                        stock = stock, availability = availability
                    )
                    searchResults.add(book)
                }

                // jika hasil pencarian tidak ditemukan
                if (result.documents.size == 0) {
                    binding.searchNotFoundImageview.visibility = View.VISIBLE
                } else {
                    binding.searchNotFoundImageview.visibility = View.GONE
                }

                // Setelah mengambil data, update RecyclerView
                adminManagementBooksAdapter.setData(searchResults)
                adminManagementBooksAdapter.notifyDataSetChanged()
                showProgressBar(false)
            }
            .addOnFailureListener { exception ->
                showProgressBar(false)
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun showProgressBar(isLoading: Boolean) {
        if (isLoading)
            binding.progressbar.visibility = View.VISIBLE
        else
            binding.progressbar.visibility = View.GONE
    }


    companion object {
        private val TAG = AdminManagementBooksActivity::class.java.simpleName
    }

}