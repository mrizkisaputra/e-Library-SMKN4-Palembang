package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.adapter.AdminManagementBooksAdapter
import sch.id.smkn4palembang.databinding.ActivityAdminManagementBooksBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.utils.AlertDialog
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference

class AdminManagementBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminManagementBooksBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertDialog: AlertDialog

    private val firestore = Firebase.firestore
    private val firebaseStorage = Firebase.storage

    private val listBook: ArrayList<Book> = ArrayList()
    private val searchResults = ArrayList<Book>()
    private lateinit var adminManagementBooksAdapter: AdminManagementBooksAdapter

    private lateinit var searchView: SearchView
    private var currentSearchCategory: String = "Judul Buku"

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
            createOptionSearchMenu(appbar.menu)
            setupSearchCategoryDropdown()

        }
    }

    /**
     * fungsi untuk mengatur kategori pencarian saat pengguna memilih dari dropdown menu
     */
    private fun setupSearchCategoryDropdown() {
        val dropdownMenu = binding.searchBookSpinner
        val categories = arrayOf("Judul Buku", "Kategori Buku", "ISBN")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdownMenu.adapter = adapter

        dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Setel kategori pencarian saat ini berdasarkan pilihan pengguna
                currentSearchCategory = categories[position]
                searchView.queryHint = getString(R.string.admin_search_books, currentSearchCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * @desc membuat menu search di appbar
     */
    private fun createOptionSearchMenu(menu: Menu) {
        val menuItem = menu.findItem(R.id.search_book_menu)
        searchView = menuItem.actionView as SearchView

        // Mengatur perilaku SearchView saat pengguna melakukan pencarian
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty() && query.isNotBlank()) {
                    searchBooks(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    getBooks()
                }
                return true
            }

        })
    }

    private fun onItemBookClick(book: Book, position: Int) {
        alertDialog.createAlertDialog(book)
        alertDialog.setOnButtonClickListener(
            {
                /**
                 * disini tempat menangani delete item
                 */
                deleteBook(book, position)
            },
            {
                /**
                 * disini tempat menangani update item
                 */
                Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun deleteBook(book: Book, position: Int) {
        val documentID = listBook[position].documentID
//        val documentIdSearchBook = searchResults[position].documentID

        // mendapatkan url image sebelum data di firestore dihapus
        val imageURL = listBook[position].cover
        Log.i(TAG, "deleteBook: $position : $documentID : ${book.isbn}")

        progressDialog.showProgressDialog()
        if (documentID != null) {
            firestore.collection(Reference.BOOKS_COLLECTION)
                .document(documentID)
                .delete()
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this, "Gagal Menghapus Data", Toast.LENGTH_SHORT).show()
                        progressDialog.dismissProgressDialog()
                    } else {

                        /**
                         * menghapus file gambar di firebase storage
                         */
                        if (!imageURL.isNullOrEmpty()) {
                            val storageRef = firebaseStorage.getReferenceFromUrl(imageURL)
                            storageRef.delete()
                                .addOnSuccessListener {
                                    Log.i(TAG, "delete imageURL: Success")
                                }
                                .addOnFailureListener {
                                    Log.i(TAG, "delete imageURL: ${it.message}")
                                }
                        }
                        progressDialog.dismissProgressDialog()
                        Toast.makeText(
                            this,
                            "Buku ${book.title} berhasil di hapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (searchResults.isEmpty()) {
                            getBooks()
                        }

                        if (searchResults.isNotEmpty()) {
                            listBook.removeAt(position)
                            searchResults.removeAt(position)
                            adminManagementBooksAdapter.setData(listBook)
                            adminManagementBooksAdapter.notifyDataSetChanged()
                        }

                    }
                }
        } else {
            Log.d(TAG, "deleteBook NULL: $position : $documentID")
        }
    }

    /**
     * @desc fungsi ini untuk menginisialisasi recyclerview
     */
    private fun initRecyclerview() {
        adminManagementBooksAdapter = AdminManagementBooksAdapter(this)
        binding.apply {
            recyclerview.adapter =
                ScaleInAnimationAdapter(adminManagementBooksAdapter).apply { setDuration(500) }
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

        firestore.collection(Reference.BOOKS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listBook.clear()

                if (!result.isEmpty) {
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
                            stock = stock, availability = availability, documentID = document.id
                        )
                        listBook.add(book)
                    }

                    binding.searchNotFound.visibility = View.GONE
                    // Setelah mengambil dan mengurutkan data, update RecyclerView
                    adminManagementBooksAdapter.setData(listBook)
                    adminManagementBooksAdapter.notifyDataSetChanged()
                    progressDialog.dismissProgressDialog()
                } else {
                    adminManagementBooksAdapter.notifyDataSetChanged()
                    binding.searchNotFound.visibility = View.VISIBLE
                    progressDialog.dismissProgressDialog()
                }
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

        val db = firestore.collection(Reference.BOOKS_COLLECTION)
        var query: Query =
            db.orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")

        when (currentSearchCategory) {
            "Kategori Buku" -> {
                query = db.orderBy("category").startAt(keyword).endAt(keyword + "\uf8ff")
            }

            "ISBN" -> {
                query = db.orderBy("isbn").startAt(keyword).endAt(keyword + "\uf8ff")
            }
        }

        query.get()
            .addOnSuccessListener { result ->
                listBook.clear()
                searchResults.clear()
                // List untuk hasil pencarian

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
                        stock = stock, availability = availability, documentID = document.id
                    )
                    searchResults.add(book)
                    listBook.add(book)
                }

                if (searchResults.isEmpty()) {
                    binding.searchNotFound.visibility = View.VISIBLE
                    // Setelah mengambil data, update RecyclerView
                    adminManagementBooksAdapter.setData(searchResults)
                    adminManagementBooksAdapter.notifyDataSetChanged()
                    showProgressBar(false)
                } else {
                    binding.searchNotFound.visibility = View.GONE
                    // Setelah mengambil data, update RecyclerView
                    adminManagementBooksAdapter.setData(searchResults)
                    adminManagementBooksAdapter.notifyDataSetChanged()
                    showProgressBar(false)
                }
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