package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.adapter.AdminManagementBooksAdapter
import sch.id.smkn4palembang.adapter.AdminManagementVisitorsAdapter
import sch.id.smkn4palembang.admin.utils.ProgressDialog
import sch.id.smkn4palembang.admin.utils.Reference
import sch.id.smkn4palembang.databinding.ActivityAdminManagementVisitorsBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.model.Visitor

class AdminManagementVisitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManagementVisitorsBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adminManagementVisitorsAdapter: AdminManagementVisitorsAdapter
    private val listVisitor = ArrayList<Visitor>()

    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementVisitorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        initRecyclerview()
        getVisitors()
    }

    /**
     * @desc fungsi ini untuk menginisialisasi recyclerview
     */
    private fun initRecyclerview() {
        adminManagementVisitorsAdapter = AdminManagementVisitorsAdapter(this)
        binding.apply {
            recyclerview.adapter = ScaleInAnimationAdapter(adminManagementVisitorsAdapter).apply { setDuration(500) }
            recyclerview.layoutManager = LinearLayoutManager(this@AdminManagementVisitorActivity)
            recyclerview.addItemDecoration(
                DividerItemDecoration(this@AdminManagementVisitorActivity, DividerItemDecoration.VERTICAL)
            )
            recyclerview.setHasFixedSize(true)
        }
    }

    /**
     * @desc fungsi ini untuk mengambil data pengunjung dan menampilkan data terbaru di paling atas
     */
    private fun getVisitors() {
        progressDialog.showProgressDialog()

        firestore.collection(Reference.VISITOR_COLLECTION)
            .orderBy("visiting_time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listVisitor.clear()

                // mengambil semua dokumen
                for (document in result) {
                    val photo = document.getString("photo")
                    val name = document.getString("name")
                    val id = document.getString("id_card")
                    val role = document.getString("role")
                    val timeStamp = document.getLong("time_stamp")
                    val visitingTime = document.getString("visiting_time")

                    val visitor = Visitor(
                        photo = photo, name = name, id = id, role = role,
                        visitingTime = visitingTime, timetamp = timeStamp
                    )
                    listVisitor.add(visitor)
                }

                // Setelah mengambil dan mengurutkan data, update RecyclerView
                adminManagementVisitorsAdapter.setData(listVisitor)
                adminManagementVisitorsAdapter.notifyDataSetChanged()
                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
            }
    }


}