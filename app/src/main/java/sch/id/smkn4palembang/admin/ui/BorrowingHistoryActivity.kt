package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.adapter.BorrowingHistoryAdapter
import sch.id.smkn4palembang.databinding.ActivityBorrowingHistoryBinding
import sch.id.smkn4palembang.model.BookBorrowing
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference

class BorrowingHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBorrowingHistoryBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: BorrowingHistoryAdapter

    private val listBorrowingHistory: ArrayList<BookBorrowing> = ArrayList()
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBorrowingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        binding.appbar.setNavigationOnClickListener { finish() }

        initRecyclerview()
        getBorrowingHistory()
    }

    private fun initRecyclerview() {
        adapter = BorrowingHistoryAdapter(this)
        val alphaAdapter = AlphaInAnimationAdapter(adapter)
        binding.recyclerview.adapter = ScaleInAnimationAdapter(alphaAdapter).apply { setDuration(500) }
        binding.recyclerview.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.recyclerview.setHasFixedSize(true)
    }

    private fun getBorrowingHistory() {
        progressDialog.showProgressDialog()

        val collection = firestore.collection(Reference.BORROWING_HISTORY_COLLECTION)
        collection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listBorrowingHistory.clear()

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
                        val timestamp = doc.getLong("timestamp")

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
                            documentID = doc.id,
                            timestamp = timestamp
                        )

                        listBorrowingHistory.add(bookBorrowing)
                    }

                    progressDialog.dismissProgressDialog()
                    adapter.setData(listBorrowingHistory)
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
                    this,
                    "terjadi masalah koneksi internet",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismissProgressDialog()
            }
    }


}