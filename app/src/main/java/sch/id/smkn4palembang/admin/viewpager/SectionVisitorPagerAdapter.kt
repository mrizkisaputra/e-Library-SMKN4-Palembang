package sch.id.smkn4palembang.admin.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import sch.id.smkn4palembang.admin.ui.fragment.VisitorFragment
import sch.id.smkn4palembang.admin.ui.fragment.VisitorStudentsFragment
import sch.id.smkn4palembang.admin.ui.fragment.VisitorTeachersFragment
import sch.id.smkn4palembang.admin.ui.fragment.VisitorTodayFragment

class SectionVisitorPagerAdapter(activity: AppCompatActivity)
    : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = VisitorFragment()
            1 -> fragment = VisitorTodayFragment()
            2 -> fragment = VisitorStudentsFragment()
            3 -> fragment = VisitorTeachersFragment()
        }

        return fragment as Fragment
    }



}