package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.adapter.AdminManagementBooksAdapter
import sch.id.smkn4palembang.adapter.AdminManagementVisitorsAdapter
import sch.id.smkn4palembang.admin.viewpager.SectionVisitorPagerAdapter
import sch.id.smkn4palembang.utils.ProgressDialog
import sch.id.smkn4palembang.utils.Reference
import sch.id.smkn4palembang.databinding.ActivityAdminManagementVisitorsBinding
import sch.id.smkn4palembang.model.Book
import sch.id.smkn4palembang.model.Visitor

class AdminManagementVisitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManagementVisitorsBinding

    companion object {

        @StringRes
        private val TAB_TITLE = intArrayOf(
            R.string.tab_title_all,
            R.string.tab_title_students_visitor,
            R.string.tab_title_teachers_visitor
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementVisitorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun onMenuItemClick(itemMenu: MenuItem) : Boolean {
        return when (itemMenu.itemId) {
            R.id.print_today_visitor_menu -> {
                Toast.makeText(this, "Print Today", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.print_all_visitor_menu -> {
                Toast.makeText(this, "Print ALl", Toast.LENGTH_SHORT).show()
                true
            }

            else -> false
        }
    }



}