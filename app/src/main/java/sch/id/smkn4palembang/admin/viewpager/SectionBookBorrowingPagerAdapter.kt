package sch.id.smkn4palembang.admin.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import sch.id.smkn4palembang.admin.ui.fragment.BookBorrowingFragment
import sch.id.smkn4palembang.admin.ui.fragment.BookingBookFragment
import sch.id.smkn4palembang.admin.ui.fragment.DueDateFragment

class SectionBookBorrowingPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null

        when (position) {
            0 -> fragment = BookBorrowingFragment()
            1 -> fragment = DueDateFragment()
            2 -> fragment = BookingBookFragment()
        }

        return fragment as Fragment
    }


}