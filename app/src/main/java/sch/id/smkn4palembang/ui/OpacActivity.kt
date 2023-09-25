package sch.id.smkn4palembang.ui

import android.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import sch.id.smkn4palembang.adapter.OpacAdapter
import sch.id.smkn4palembang.databinding.ActivityOpacBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.utils.Reference

class OpacActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpacBinding
    private lateinit var adapterNewCollection: OpacAdapter
    private lateinit var adapterPopularCollection: OpacAdapter

    private val listCatalogNewCollection: MutableList<Book> = mutableListOf()
    private val listCatalogPopulerCollection: MutableList<Book> = mutableListOf()
    private var currentSearchCatalog = "Judul Buku"
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpacBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerview()
        getBooksNewCollection()
        getBooksPopularCollection()

        adapterNewCollection.setOnItemClickListener { book, i ->
            Toast.makeText(this, "Terbaru: ${book.title} $i", Toast.LENGTH_SHORT).show()
        }

        adapterPopularCollection.setOnItemClickListener { book, i ->
            Toast.makeText(this, "Populer: ${book.title} $i", Toast.LENGTH_SHORT).show()
        }

        binding.apply {
            setupSearchCatalogDropdown()
            searchCatalogButton.setOnClickListener {
                searchCatalogBooks()
            }

        }
    }

    private fun searchCatalogBooks() {
        val keyword = binding.searchCatalogEdittext.text.toString().trim()

        if (keyword.isNotEmpty()) {

        } else {
            // jika field edittext kosong
        }

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
                binding.searchCatalogTextinputlayout.hint =
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
                        val timestamp = doc.get("timestamp")

                        val book = Book(
                            cover = cover, title = title, publisher = publisher, isbn = isbn,
                            availability = availability, category = category, description = description,
                            language = language, stock = stock, timestamp = timestamp.toString()
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
                        val timestamp = doc.get("timestamp")

                        val book = Book(
                            cover = cover, title = title, publisher = publisher, isbn = isbn,
                            availability = availability, category = category, description = description,
                            language = language, stock = stock, timestamp = timestamp.toString()
                        )

                        listCatalogPopulerCollection.add(book)
                    }

                    adapterPopularCollection.setData(listCatalogPopulerCollection)
                    adapterPopularCollection.notifyDataSetChanged()
                }
            }


    }

}