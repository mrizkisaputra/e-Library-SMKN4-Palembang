package sch.id.smkn4palembang.ui

import android.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.adapter.OpacAdapter
import sch.id.smkn4palembang.adapter.OpacSearchResultAdapter
import sch.id.smkn4palembang.admin.ui.AdminManagementBooksActivity
import sch.id.smkn4palembang.databinding.ActivityDetailOpacBinding
import sch.id.smkn4palembang.databinding.ActivityOpacBinding
import sch.id.smkn4palembang.databinding.GridItemHorizontalOpacBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference

class OpacActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpacBinding
    private lateinit var adapterNewCollection: OpacAdapter
    private lateinit var adapterPopularCollection: OpacAdapter
    private lateinit var adapterSearchCollection: OpacSearchResultAdapter
    private lateinit var progressDialog: ProgressDialog

    private val listCatalogNewCollection: MutableList<Book> = mutableListOf()
    private val listCatalogPopulerCollection: MutableList<Book> = mutableListOf()
    private val listSearchResults: MutableList<Book> = mutableListOf()
    private var currentSearchCatalog = "Judul Buku"
    private val firestore = Firebase.firestore

    companion object {
        const val EXTRA_BOOK = "extra book"
        const val EXTRA_POSITION = "extra position"
        private val TAG = OpacActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpacBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        initRecyclerview()
        getBooksNewCollection()
        getBooksPopularCollection()

        adapterNewCollection.setOnItemClickListener { book, position ->
            toDetailOpac(book, position)
        }
        adapterPopularCollection.setOnItemClickListener { book, position ->
            toDetailOpac(book, position)
        }
        adapterSearchCollection.setOnItemClickListener { book, position ->
            toDetailOpac(book, position)
        }

        binding.apply {
            setupSearchCatalogDropdown()
            catalogSearchview.setupWithSearchBar(catalogSearchbar)
            catalogSearchview.isAnimatedNavigationIcon = true
            catalogSearchview.editText
                .setOnEditorActionListener { textView, actionId, keyEvent ->
                    val keyword = textView.text.toString().trim().lowercase()

                    if (keyword.isNotBlank()) {
                        searchCatalog(keyword)
                        listSearchResults.clear()

                    }



                    true
                }


        }
    }

    private fun toDetailOpac(book: Book, position: Int) {
        val intent = Intent(this, DetailOpacActivity::class.java).apply {
            putExtra(EXTRA_BOOK, book)
            putExtra(EXTRA_POSITION, position)
        }
        startActivity(intent)
    }

    /**
     * pencarian catalog buku berdasarkan kategori pilihan
     */
    private fun setupSearchCatalogDropdown() {
        val spinner = binding.searchBySpinner
        val categories = arrayOf("Judul Buku", "Kategori Buku", "ISBN/ISSN")

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Setel kategori pencarian saat ini berdasarkan pilihan pengguna
                currentSearchCatalog = categories[position]
                binding.catalogSearchbar.hint =
                    getString(sch.id.smkn4palembang.R.string.search_catalog_by, currentSearchCatalog)
                binding.catalogSearchview.hint =
                    getString(sch.id.smkn4palembang.R.string.search_catalog_by, currentSearchCatalog)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun initRecyclerview() {

        // buat adapter untuk catalog new collection
        adapterNewCollection = OpacAdapter(this)
        binding.catalogNewCollectionRecyclerview.adapter = AlphaInAnimationAdapter(adapterNewCollection).apply { setDuration(500) }
        binding.catalogNewCollectionRecyclerview.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        binding.catalogNewCollectionRecyclerview.setHasFixedSize(true)

        // buat adapter untuk catalog popular collection
        adapterPopularCollection = OpacAdapter(this)
        binding.catalogPopularRecyclerview.adapter = AlphaInAnimationAdapter(adapterPopularCollection).apply { setDuration(700) }
        binding.catalogPopularRecyclerview.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)


        adapterSearchCollection = OpacSearchResultAdapter(this)
        binding.catalogSearchRecyclerview.adapter = ScaleInAnimationAdapter(
            AlphaInAnimationAdapter(adapterSearchCollection)
        ).apply { setDuration(500) }
        binding.catalogSearchRecyclerview.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun searchCatalog(keyword: String) {
        progressDialog.showProgressDialog()

        val db = firestore.collection(Reference.BOOKS_COLLECTION)

        when (currentSearchCatalog) {
            "Kategori Buku" -> {
                db.get()
                    .addOnSuccessListener { result ->
                        for (doc in result) {
                            val categoryKeyword = doc.getString("category")

                            if (categoryKeyword != null && categoryKeyword.contains(keyword)) {
                                val cover = doc.getString("cover")
                                val title = doc.getString("title")
                                val publisher = doc.getString("publisher")
                                val isbn = doc.getString("isbn")
                                val availability = doc.getString("availability")
                                val category = doc.getString("category")
                                val description = doc.getString("description")
                                val language = doc.getString("language")
                                val stock = doc.getString("stok")
                                val timestamp = doc.getTimestamp("timestamp")

                                val book = Book(
                                    cover = cover, title = title, publisher = publisher, isbn = isbn,
                                    availability = availability, category = category, description = description,
                                    language = language, stock = stock, timestamp = timestamp
                                )

                                listSearchResults.add(book)
                            }
                        }

                        if (listSearchResults.isNotEmpty()) {
                            binding.catalogSearchEmptyImageview.visibility = View.GONE

                            progressDialog.dismissProgressDialog()
                            adapterSearchCollection.setData(listSearchResults)
                            adapterSearchCollection.notifyDataSetChanged()
                        } else {
                            progressDialog.dismissProgressDialog()
                            adapterSearchCollection.notifyDataSetChanged()
                            binding.catalogSearchEmptyImageview.visibility = View.VISIBLE
                        }
                    }
                    .addOnCompleteListener {

                    }
            }

            "ISBN/ISSN" -> {
                db.orderBy("isbn")
                    .startAt(keyword)
                    .endAt(keyword + "\uf8ff")
                    .get()
                    .addOnSuccessListener { result ->
                        listSearchResults.clear()
                        for (doc in result) {
                            val cover = doc.getString("cover")
                            val title = doc.getString("title")
                            val publisher = doc.getString("publisher")
                            val isbn = doc.getString("isbn")
                            val availability = doc.getString("availability")
                            val category = doc.getString("category")
                            val description = doc.getString("description")
                            val language = doc.getString("language")
                            val stock = doc.getString("stok")
                            val timestamp = doc.getTimestamp("timestamp")

                            val book = Book(
                                cover = cover, title = title, publisher = publisher, isbn = isbn,
                                availability = availability, category = category, description = description,
                                language = language, stock = stock, timestamp = timestamp
                            )

                            listSearchResults.add(book)
                        }

                        if (listSearchResults.isNotEmpty()) {
                            binding.catalogSearchEmptyImageview.visibility = View.GONE

                            progressDialog.dismissProgressDialog()
                            adapterSearchCollection.setData(listSearchResults)
                            adapterSearchCollection.notifyDataSetChanged()
                        } else {
                            progressDialog.dismissProgressDialog()
                            adapterSearchCollection.notifyDataSetChanged()
                            binding.catalogSearchEmptyImageview.visibility = View.VISIBLE
                        }


                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                    }
            }

            else -> {
                db.get()
                    .addOnSuccessListener { result ->
                        listSearchResults.clear()
                        for (doc in result) {
                            val titleKeyword = doc.getString("search_title")

                            if (titleKeyword != null && titleKeyword.contains(keyword)) {
                                val cover = doc.getString("cover")
                                val title = doc.getString("title")
                                val publisher = doc.getString("publisher")
                                val isbn = doc.getString("isbn")
                                val availability = doc.getString("availability")
                                val category = doc.getString("category")
                                val description = doc.getString("description")
                                val language = doc.getString("language")
                                val stock = doc.getString("stok")
                                val timestamp = doc.getTimestamp("timestamp")

                                val book = Book(
                                    cover = cover, title = title, publisher = publisher, isbn = isbn,
                                    availability = availability, category = category, description = description,
                                    language = language, stock = stock, timestamp = timestamp
                                )

                                listSearchResults.add(book)
                            }
                        }

                        if (listSearchResults.isNotEmpty()) {
                            binding.catalogSearchEmptyImageview.visibility = View.GONE

                            progressDialog.dismissProgressDialog()
                            adapterSearchCollection.setData(listSearchResults)
                            adapterSearchCollection.notifyDataSetChanged()
                        } else {
                            progressDialog.dismissProgressDialog()
                            adapterSearchCollection.notifyDataSetChanged()
                            binding.catalogSearchEmptyImageview.visibility = View.VISIBLE
                        }


                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                    }
            }




        }
    }


    /**
     * mengambil katalog koleksi terbaru
     */
    private fun getBooksNewCollection() {

        val collectionRef = firestore.collection(Reference.BOOKS_COLLECTION)
        collectionRef
            .limit(15)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if (error != null) {
                    // jika error mau ngapain
                    Toast.makeText(
                        this,
                        "Terjadi masalah koneksi internet",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                listCatalogNewCollection.clear()
                if (value != null) {
                    binding.catalogEmptyImageview.visibility = View.GONE
                    for (doc in value.documents) {
                        val cover = doc.getString("cover")
                        val title = doc.getString("title")
                        val publisher = doc.getString("publisher")
                        val isbn = doc.getString("isbn")
                        val availability = doc.getString("availability")
                        val category = doc.getString("category")
                        val description = doc.getString("description")
                        val language = doc.getString("language")
                        val stock = doc.getString("stok")
                        val timestamp = doc.getTimestamp("timestamp")

                        val book = Book(
                            cover = cover, title = title, publisher = publisher, isbn = isbn,
                            availability = availability, category = category, description = description,
                            language = language, stock = stock, timestamp = timestamp
                        )

                        listCatalogNewCollection.add(book)
                    }

                    adapterNewCollection.setData(listCatalogNewCollection)
                    adapterNewCollection.notifyDataSetChanged()
                } else {
                    binding.catalogEmptyImageview.visibility = View.VISIBLE
                }
            }
    }

    /**
     * mengambil catalog koleksi populer
     */
    private fun getBooksPopularCollection() {

        val collectionRef = firestore.collection(Reference.BOOKS_COLLECTION)
        collectionRef
            .limit(15)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->

                if (error != null) {
                    // jika error mau ngapain
                    Toast.makeText(
                        this,
                        "Terjadi masalah koneksi internet",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                listCatalogPopulerCollection.clear()
                if (value != null) {
                    for (doc in value.documents) {
                        val cover = doc.getString("cover")
                        val title = doc.getString("title")
                        val publisher = doc.getString("publisher")
                        val isbn = doc.getString("isbn")
                        val availability = doc.getString("availability")
                        val category = doc.getString("category")
                        val description = doc.getString("description")
                        val language = doc.getString("language")
                        val stock = doc.getString("stok")
                        val timestamp = doc.getTimestamp("timestamp")

                        val book = Book(
                            cover = cover, title = title, publisher = publisher, isbn = isbn,
                            availability = availability, category = category, description = description,
                            language = language, stock = stock, timestamp = timestamp
                        )

                        listCatalogPopulerCollection.add(book)
                    }

                    adapterPopularCollection.setData(listCatalogPopulerCollection)
                    adapterPopularCollection.notifyDataSetChanged()
                }
            }


    }

}