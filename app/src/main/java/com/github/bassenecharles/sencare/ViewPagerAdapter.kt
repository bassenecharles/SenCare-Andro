package com.github.bassenecharles.sencare

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bassenecharles.sencare.ui.AccueilFragment
import com.github.bassenecharles.sencare.ui.FormulesFragment
import com.github.bassenecharles.sencare.ui.PatientListFragment
import com.github.bassenecharles.sencare.ui.ParametresFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AccueilFragment()
            1 -> PatientListFragment()
            2 -> FormulesFragment()
            3 -> ParametresFragment()
            else -> AccueilFragment()
        }
    }
}
