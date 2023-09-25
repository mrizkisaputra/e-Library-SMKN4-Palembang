package sch.id.smkn4palembang.admin.ui

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import sch.id.smkn4palembang.R
import sch.id.smkn4palembang.admin.viewpager.SectionBookBorrowingPagerAdapter
import sch.id.smkn4palembang.databinding.ActivityAdminManagementBorrowBookBinding

class AdminManagementBorrowBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminManagementBorrowBookBinding
    private lateinit var viewPager: SectionBookBorrowingPagerAdapter

    companion object {

        @StringRes
        private val TAB_LAYOUT_TITLE = listOf(
            R.string.tab_title_borrowing,
            R.string.tab_title_borrowing_due_date,
            R.string.tab_title_booking_book
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManagementBorrowBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPagerAdapter()

    }

    private fun initPagerAdapter() {
        viewPager = SectionBookBorrowingPagerAdapter(this)
        binding.viewPager.adapter = viewPager

        TabLayoutMediator(binding.tablayout, binding.viewPager) { tab, position ->
            tab.text = getString(TAB_LAYOUT_TITLE[position])
        }.attach()
    }


}