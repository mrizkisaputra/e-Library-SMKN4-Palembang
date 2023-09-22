package sch.id.smkn4palembang.admin.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.adapter.AdminManagementMembersAdapter
import sch.id.smkn4palembang.databinding.ActivityAdminManagementMemberBinding
import sch.id.smkn4palembang.model.Member
import sch.id.smkn4palembang.utils.AlertDialog
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AdminManagementMemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManagementMemberBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var alertDialog: AlertDialog

    private lateinit var adminManagementMemberAdapter: AdminManagementMembersAdapter
    private val listMembers = ArrayList<Member>()
    private val searchResult = ArrayList<Member>()

    private val firestore = Firebase.firestore
    private val firebaseStorage = Firebase.storage

    private lateinit var workbook: XSSFWorkbook

    companion object {
        private val TAG = AdminManagementMemberActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        alertDialog = AlertDialog(this)
        workbook = XSSFWorkbook()

        initRecyclerview()
        getMembers()

        adminManagementMemberAdapter.setOnItemClickListener(::onItemMemberClick)

        createOptionSearchMenu(binding.appbar.menu)

        binding.appbar.setOnMenuItemClickListener(::onMenuItemClick)
    }

    private fun createOptionSearchMenu(menu: Menu) {
        val menuItem = menu.findItem(R.id.search_member_menu)
        val searchView = menuItem.actionView as SearchView

        searchView.queryHint = "Cari berdasarkan id anggota"
        searchView.inputType = InputType.TYPE_CLASS_NUMBER

        // Mengatur perilaku SearchView saat pengguna melakukan pencarian
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty() && query.isNotBlank()) {
                    searchMembers(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText.isNullOrEmpty()) {
                    getMembers()
                }
                return true
            }

        })
    }

    private fun searchMembers(memberId: String) {
        binding.progressbar.visibility = View.VISIBLE

        firestore.collection(Reference.MEMBERS_COLLECTION)
            .orderBy("id")
            .startAt(memberId)
            .endAt(memberId + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                binding.progressbar.visibility = View.GONE

                listMembers.clear()
                searchResult.clear()

                // mengambil semua dokumen dari pencarian
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
                    searchResult.add(member)
                    listMembers.add(member)
                }

                if (searchResult.isNotEmpty()) {
                    binding.memberIsEmptyImageview.visibility = View.GONE
                    adminManagementMemberAdapter.setData(searchResult)
                    adminManagementMemberAdapter.notifyDataSetChanged()
                } else {
                    binding.memberIsEmptyImageview.visibility = View.VISIBLE
                    adminManagementMemberAdapter.setData(searchResult)
                    adminManagementMemberAdapter.notifyDataSetChanged()
                }

            }
            .addOnFailureListener {

            }
    }

    private fun onMenuItemClick(itemMenu: MenuItem): Boolean {
        return when (itemMenu.itemId) {
            R.id.print_all_member_menu -> {
                fetchAllMemberFromFirestore()
                true
            }

            else -> false
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
                    setPositiveButton("Yakin") { _, _ ->
                        deleteMember(member, position) { photoURL ->
                            val storageRef = firebaseStorage.getReferenceFromUrl(photoURL)
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

                        if (searchResult.isEmpty()) {
                            getMembers()
                        }

                        if (searchResult.isNotEmpty()) {
                            listMembers.removeAt(position)
                            searchResult.removeAt(position)
                            adminManagementMemberAdapter.setData(listMembers)
                            adminManagementMemberAdapter.notifyDataSetChanged()
                        }
                    }
                }

        }.run {
            Log.e(TAG, "documentID: NULL")
        }
    }

    private fun initRecyclerview() {
        adminManagementMemberAdapter = AdminManagementMembersAdapter(this)
        binding.apply {
            recyclerview.adapter =
                ScaleInAnimationAdapter(adminManagementMemberAdapter).apply { setDuration(500) }
            recyclerview.layoutManager = LinearLayoutManager(this@AdminManagementMemberActivity)
            recyclerview.addItemDecoration(
                DividerItemDecoration(
                    this@AdminManagementMemberActivity,
                    DividerItemDecoration.VERTICAL
                )
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
                    progressDialog.dismissProgressDialog()
                }

                progressDialog.dismissProgressDialog()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismissProgressDialog()
            }
    }


    private fun fetchAllMemberFromFirestore() {
        val firestore = Firebase.firestore

        firestore
            .collection(Reference.MEMBERS_COLLECTION)
            .orderBy("date_time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(
                        this,
                        "data member kosong, tidak bisa di export",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    exportAllMemberToExcel(result)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "periksa koneksi internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun exportAllMemberToExcel(data: QuerySnapshot) {
        val existingSheetIndex = workbook.getSheetIndex("Data Member Perpustakaan")
        if (existingSheetIndex != -1) workbook.removeSheetAt(existingSheetIndex)

        val sheet = workbook.createSheet("Data Member Perpustakaan")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("NO")
        headerRow.createCell(1).setCellValue("ID ANGGOTA")
        headerRow.createCell(2).setCellValue("NAMA")
        headerRow.createCell(3).setCellValue("KONTAK")
        headerRow.createCell(4).setCellValue("TANGGAL PENDAFTARAN")

        var rowNumber = 1
        for (document in data) {
            val row = sheet.createRow(rowNumber++)
            row.createCell(0).setCellValue((rowNumber - 1).toString())
            row.createCell(1).setCellValue(document.getString("id"))
            row.createCell(2).setCellValue(document.getString("name"))
            row.createCell(3).setCellValue(document.getString("contact"))
            row.createCell(4).setCellValue(document.getString("date_time"))
        }

        try {
            val file = File(getExternalFilesDir(null), "Data Anggota Perpustakaan.xlsx")
            val fileOutputStream = FileOutputStream(file)
            workbook.write(fileOutputStream)
            fileOutputStream.close()

            // Membuka file Excel dengan aplikasi yang sesuai
            val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                fileUri,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            intent.flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION // Mengizinkan aplikasi lain untuk membaca file

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Handle jika tidak ada aplikasi yang dapat membuka file Excel
                Toast.makeText(
                    this,
                    "tidak ada aplikasi yang dapat membuka file excel",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IOException) {
            Log.e("Excel", "Error exporting data to Excel: $e")
            Toast.makeText(this, "gagal mengekspor data ke Excel", Toast.LENGTH_SHORT).show()
        }
    }

}