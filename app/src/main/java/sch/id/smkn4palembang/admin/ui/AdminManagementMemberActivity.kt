package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.adapter.AdminManagementMembersAdapter
import sch.id.smkn4palembang.adapter.AdminManagementVisitorsAdapter
import sch.id.smkn4palembang.utils.AlertDialog
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.utils.SuccessDialog
import sch.id.smkn4palembang.databinding.ActivityAdminManagementMemberBinding
import sch.id.smkn4palembang.model.Member
import sch.id.smkn4palembang.model.Visitor

class AdminManagementMemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManagementMemberBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertDialog: AlertDialog
    private lateinit var adminManagementMemberAdapter: AdminManagementMembersAdapter
    private val listMembers = ArrayList<Member>()
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        alertDialog = AlertDialog(this)

        initRecyclerview()
        getMembers()

        adminManagementMemberAdapter.setOnItemClickListener { item ->
            alertDialog.createAlertDialog(item)

            alertDialog.setOnButtonClickListener(
                {
                    // delete member
                },
                {
                    // update member
                }
            )
        }

        binding.apply {
            searchView.editText.setOnEditorActionListener { textView, i, keyEvent ->
                //lakukan pencarian data disini
                true
            }
        }

    }

    private fun initRecyclerview() {
        adminManagementMemberAdapter = AdminManagementMembersAdapter(this)
        binding.apply {
            recyclerview.adapter = ScaleInAnimationAdapter(adminManagementMemberAdapter).apply { setDuration(500) }
            recyclerview.layoutManager = LinearLayoutManager(this@AdminManagementMemberActivity)
            recyclerview.addItemDecoration(
                DividerItemDecoration(this@AdminManagementMemberActivity, DividerItemDecoration.VERTICAL)
            )
            recyclerview.setHasFixedSize(true)
        }
    }

    /**
     * @desc fungsi ini untuk mengambil data pengunjung dan menampilkan data terbaru di paling atas
     */
    private fun getMembers() {
        progressDialog.showProgressDialog()

        firestore.collection(Reference.MEMBERS_COLLECTION)
            .orderBy("date_time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listMembers.clear()

                if (!result.isEmpty) {
                    // mengambil semua dokumen
                    for (document in result) {
                        val photo = document.getString("photo")
                        val name = document.getString("name")
                        val id = document.getString("id")
                        val password = document.getString("password")
                        val dateTime = document.getString("date_time")
                        val contact = document.getString("contact")

                        val member = Member(
                            photo = photo, name = name, id = id, contact = contact,
                            password = password, dateTime = dateTime
                        )
                        listMembers.add(member)
                    }

                    // Setelah mengambil dan mengurutkan data, update RecyclerView
                    adminManagementMemberAdapter.setData(listMembers)
                    adminManagementMemberAdapter.notifyDataSetChanged()
                    progressDialog.dismissProgressDialog()
                } else {
                    Toast.makeText(this, "Tidak Ada Data Anggota, Database Kosong", Toast.LENGTH_LONG).show()
                }

                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
            }
    }

}