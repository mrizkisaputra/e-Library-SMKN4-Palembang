package sch.id.smkn4palembang.admin.ui

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
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
    private val firebaseStorage = Firebase.storage

    companion object {
        private val TAG = AdminManagementMemberActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        alertDialog = AlertDialog(this)

        initRecyclerview()
        getMembers()

        adminManagementMemberAdapter.setOnItemClickListener(::onItemMemberClick)

        binding.apply {

        }

    }

    private fun onItemMemberClick(member: Member, position: Int) {
        alertDialog.createAlertDialog(member)

        alertDialog.setOnButtonClickListener(
            {
                /**
                 * Delete Anggota
                 */

                val builder = androidx.appcompat.app.AlertDialog.Builder(this).apply {
                    setMessage(getString(R.string.confirm_delete_data, member.id))
                    setNegativeButton("Tidak") { dialog, _ -> dialog?.dismiss() }
                    setPositiveButton("Yakin") {_, _ ->
                        deleteMember(member, position) {photoURL ->
                            val storageRef =firebaseStorage.getReferenceFromUrl(photoURL)
                            storageRef.delete()
                        }
                    }
                }.create()
                builder.show()
            },
            {
                /**
                 * Ubah Data Anggota
                 */
            }
        )
    }

    private fun deleteMember(member: Member, position: Int, deletePhoto: (String) -> Unit) {
        progressDialog.showProgressDialog()

        // mengambil id document berdasarkan posisi data di klik untuk mengahapus data anggota
        val documentID = listMembers[position].documentID

        // sebelum data di firestore dihapus kita ambil dulu url photo
        val photoURL = listMembers[position].photo

        // jika tidak null
        documentID?.let { document ->
            firestore
                .collection(Reference.MEMBERS_COLLECTION)
                .document(document)
                .delete()
                .addOnCompleteListener { task ->
                    if (!task.isComplete) {
                        progressDialog.dismissProgressDialog()
                        Toast.makeText(
                            this,
                            "Anggota ${member.name} Gagal dihapus",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        if (!photoURL.isNullOrEmpty()) {
                            deletePhoto(photoURL)
                        }

                        progressDialog.dismissProgressDialog()
                        Toast.makeText(
                            this,
                            "Anggota ${member.name} Berhasil dihapus",
                            Toast.LENGTH_LONG
                        ).show()
                        getMembers()
                    }
                }

        }.run {
            Log.e(TAG, "documentID: NULL", )
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
                    binding.memberIsEmptyImageview.visibility = View.GONE

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
                            password = password, dateTime = dateTime, documentID = document.id
                        )
                        listMembers.add(member)
                    }

                    // Setelah mengambil dan mengurutkan data, update RecyclerView
                    adminManagementMemberAdapter.setData(listMembers)
                    adminManagementMemberAdapter.notifyDataSetChanged()
                    progressDialog.dismissProgressDialog()
                } else {
                    adminManagementMemberAdapter.notifyDataSetChanged()

                    binding.memberIsEmptyImageview.visibility = View.VISIBLE
//                    Toast.makeText(this, "Tidak Ada Data Anggota, Database Kosong", Toast.LENGTH_LONG).show()
                }

                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
            }
    }

}