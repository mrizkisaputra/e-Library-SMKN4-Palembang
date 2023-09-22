package sch.id.smkn4palembang.member.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.databinding.ActivityHomeMemberBinding
import sch.id.smkn4palembang.ui.MainActivity

class HomeMemberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeMemberBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchBar.setOnMenuItemClickListener(::onItemMenuClick)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun onItemMenuClick(itemMenu: MenuItem): Boolean {
        if (itemMenu.itemId == R.id.logout_member_menu) {
            logOut()
        }

        return true
    }

    private fun logOut() {
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.logout_message_member))
            setPositiveButton(getString(R.string.logout)) { _, _ ->
                auth.signOut()
                Intent(
                    this@HomeMemberActivity,
                    MainActivity::class.java
                ).apply { startActivity(this) }
                finish()
            }
            setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
        }.create().show()
    }

}