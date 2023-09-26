package sch.id.smkn4palembang.admin.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.admin.viewpager.SectionVisitorPagerAdapter
import sch.id.smkn4palembang.databinding.ActivityAdminManagementVisitorsBinding
import sch.id.smkn4palembang.utils.Reference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdminManagementVisitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManagementVisitorsBinding

    private lateinit var workbook: XSSFWorkbook
    private val date = LocalDate
        .now(ZoneId.of("Asia/Jakarta"))
        .format(DateTimeFormatter.ofPattern("yyyyMMd"))

    companion object {

        @StringRes
        private val TAB_TITLE = intArrayOf(
            R.string.tab_title_all,
            R.string.tab_title_today_visitor,
            R.string.tab_title_students_visitor,
            R.string.tab_title_teachers_visitor
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementVisitorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workbook = XSSFWorkbook()

        binding.apply {

            appbar.setOnMenuItemClickListener(::onMenuItemClick)

            val sectionVisitorPagerAdapter =
                SectionVisitorPagerAdapter(this@AdminManagementVisitorActivity)
            viewPager.adapter = sectionVisitorPagerAdapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = getString(TAB_TITLE[position])
            }.attach()
        }


    }

    private fun onMenuItemClick(itemMenu: MenuItem): Boolean {
        return when (itemMenu.itemId) {
            R.id.print_today_visitor_menu -> {
                // export data hari ini di excel
                fetchTodayVisitorFromFirestore()
                true
            }

            R.id.print_all_visitor_menu -> {
                // export semua data di excel
                fetchAllVisitorFromFirestore()
                true
            }

            else -> false
        }
    }

    private fun exportAllVisitorToExcel(data: QuerySnapshot) {
        val existingSheetIndex = workbook.getSheetIndex("Data Pengunjung")
        if (existingSheetIndex != -1) workbook.removeSheetAt(existingSheetIndex)

        val sheet = workbook.createSheet("Data Pengunjung")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("NO")
        headerRow.createCell(1).setCellValue("ID ANGGOTA")
        headerRow.createCell(2).setCellValue("NAMA")
        headerRow.createCell(3).setCellValue("STATUS")
        headerRow.createCell(4).setCellValue("WAKTU KUNJUNGAN")

        var rowNumber = 1
        for (document in data) {
            val row = sheet.createRow(rowNumber++)
            row.createCell(0).setCellValue((rowNumber - 1).toString())
            row.createCell(1).setCellValue(document.getString("id_card"))
            row.createCell(2).setCellValue(document.getString("name"))
            row.createCell(3).setCellValue(document.getString("role"))
            row.createCell(4).setCellValue(document.getString("visiting_time"))
        }

        try {
            val file = File(getExternalFilesDir(null), "Semua Data Pengunjung Perpustakaan.xlsx")
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
                Snackbar.make(
                    binding.infoCoordinator,
                    "tidak ada aplikasi yang dapat membuka file excel",
                    Snackbar.LENGTH_SHORT
                )
                    .setBackgroundTint(getColor(R.color.md_theme_light_error))
                    .show()
            }
        } catch (e: IOException) {
            Log.e("Excel", "Error exporting data to Excel: $e")
            Toast.makeText(this, "gagal mengekspor data ke Excel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportTodayVisitorToExcel(data: QuerySnapshot) {
        val existingSheetIndex = workbook.getSheetIndex("Data Pengunjung")
        if (existingSheetIndex != -1) workbook.removeSheetAt(existingSheetIndex)

        val sheet = workbook.createSheet("Data Pengunjung")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("NO")
        headerRow.createCell(1).setCellValue("ID ANGGOTA")
        headerRow.createCell(2).setCellValue("NAMA")
        headerRow.createCell(3).setCellValue("STATUS")
        headerRow.createCell(4).setCellValue("WAKTU KUNJUNGAN")

        var rowNumber = 1
        for (document in data) {
            val row = sheet.createRow(rowNumber++)
            row.createCell(0).setCellValue((rowNumber - 1).toString())
            row.createCell(1).setCellValue(document.getString("id_card"))
            row.createCell(2).setCellValue(document.getString("name"))
            row.createCell(3).setCellValue(document.getString("role"))
            row.createCell(4).setCellValue(document.getString("visiting_time"))
        }

        try {
            val file = File(getExternalFilesDir(null), "Data Pengunjung Perpustakaan_$date.xlsx")
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
                Snackbar.make(
                    binding.infoCoordinator,
                    "tidak ada aplikasi yang dapat membuka file excel",
                    Snackbar.LENGTH_SHORT
                )
                    .setBackgroundTint(getColor(R.color.md_theme_light_error))
                    .show()
            }
        } catch (e: IOException) {
            Log.e("Excel", "Error exporting data to Excel: $e")
            Toast.makeText(this, "gagal mengekspor data ke Excel", Toast.LENGTH_SHORT).show()
        }

    }

    private fun fetchAllVisitorFromFirestore() {
        val firestore = Firebase.firestore

        firestore
            .collection(Reference.VISITOR_COLLECTION)
            .orderBy("visiting_time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Snackbar.make(
                        binding.infoCoordinator,
                        "data kosong, tidak bisa di Export",
                        Snackbar.LENGTH_SHORT
                    )
                        .setBackgroundTint(getColor(R.color.md_theme_light_error))
                        .show()
                } else {
                    exportAllVisitorToExcel(result)
                }
            }
            .addOnFailureListener {

            }
    }

    private fun fetchTodayVisitorFromFirestore() {
        val firestore = Firebase.firestore

        // Hitung waktu mulai hari ini (midnight) hingga saat ini dalam milisekon
        val currentTimeMillis = System.currentTimeMillis()
        val midnightTimeMillis = currentTimeMillis - (currentTimeMillis % (24 * 60 * 60 * 1000))

        firestore
            .collection(Reference.VISITOR_COLLECTION)
            .whereGreaterThanOrEqualTo("time_stamp", midnightTimeMillis)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Snackbar.make(
                        binding.infoCoordinator,
                        "data pengunjung hari ini kosong, tidak bisa di Export",
                        Snackbar.LENGTH_SHORT
                    )
                        .setBackgroundTint(getColor(R.color.md_theme_light_error))
                        .show()
                } else {
                    exportTodayVisitorToExcel(result)
                }
            }
            .addOnFailureListener {
                Log.d("GETT", "GAGAL MENDAPATKAN DATA: ${it.message}")
            }
    }

}