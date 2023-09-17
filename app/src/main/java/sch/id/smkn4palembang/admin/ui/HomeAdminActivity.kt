package sch.id.smkn4palembang.admin.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityHomeAdminBinding
import sch.id.smkn4palembang.ui.MainActivity

class HomeAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // info login berhasil
        Snackbar.make(
            binding.infoLoginCoordinatorLayout,
            getString(R.string.success_info_login_admin),
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(getColor(R.color.md_theme_light_primary)).show()

        binding.booksCardview.setOnClickListener(::onClick)
        binding.membersCardview.setOnClickListener(::onClick)
        binding.visitorsCardview.setOnClickListener(::onClick)
        binding.borrowBookCardview.setOnClickListener(::onClick)
        binding.logoutCardview.setOnClickListener(::onClick)

        binding.topAppBar.setOnMenuItemClickListener { onItemMenuAppBarClick(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun onItemMenuAppBarClick(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.management_books_menu -> {
                Intent(this, AdminManagementBooksActivity::class.java).apply { startActivity(this) }
                true
            }

            R.id.management_members_menu -> {
                Intent(this, AdminManagementMemberActivity::class.java).apply { startActivity(this) }
                true
            }

            R.id.management_visitors_menu -> {
                Intent(this, AdminManagementVisitorActivity::class.java).apply { startActivity(this) }
                true
            }

            R.id.management_borrow_menu -> {
                true
            }

            R.id.announcement_menu -> {
                true
            }

            else -> false
        }
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.books_cardview -> {
                Intent(this, AdminInsertBookActivity::class.java).apply { startActivity(this) }
            }

            R.id.members_cardview -> {
                Intent(this, AdminInsertMemberActivity::class.java).apply { startActivity(this) }
            }

            R.id.visitors_cardview -> {
                Intent(this, AdminInsertVisitorActivity::class.java).apply { startActivity(this) }
            }

            R.id.borrow_book_cardview -> {  }

            R.id.logout_cardview -> logOut()
        }
    }

    private fun logOut() {
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.logout_message))
            setPositiveButton(getString(R.string.logout)) { _, _ ->
                Firebase.auth.signOut()
                Intent(this@HomeAdminActivity, MainActivity::class.java).apply { startActivity(this) }
                finish()
            }
            setNegativeButton("Tidak") { dialog, _ ->  dialog.dismiss() }
        }.create().show()
    }


}