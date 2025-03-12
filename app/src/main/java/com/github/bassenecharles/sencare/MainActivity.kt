package com.github.bassenecharles.sencare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.github.bassenecharles.sencare.ui.AccueilFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.github.bassenecharles.sencare.ui.PatientListFragment
import com.github.bassenecharles.sencare.ui.FormulesFragment

interface PatientListListener {
    fun updateTotalPatients()
}

interface DataImportListener {
    fun onDataImported(activity: MainActivity)
}

class MainActivity : AppCompatActivity(), PatientListListener, DataImportListener {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var accueilFragment: AccueilFragment
    private lateinit var adapter: ViewPagerAdapter

    @Deprecated("Deprecated, please use another method")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Accueil"
                1 -> tab.text = "Liste de Patients"
                2 -> tab.text = "Formules"
                3 -> tab.text = "Paramètres"
            }
        }.attach()

        // Get reference to AccueilFragment
        accueilFragment = adapter.createFragment(0) as AccueilFragment
        updateTotalPatients()
    }

    @Deprecated("Deprecated, please use another method")
    override fun updateTotalPatients() {
        if (::accueilFragment.isInitialized) {
            accueilFragment.loadTotalPatients()
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated, please use another method")
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is PatientListListener) {
            @Suppress("DEPRECATION")
            patientListListener = fragment
        }
    }

    private lateinit var patientListListener: PatientListListener

    override fun onDataImported(activity: MainActivity) {
        @Suppress("DEPRECATION")
        updateTotalPatients()
        val patientListFragment = getPatientListFragment()
        patientListFragment?.let {
            it.loadPatients()
        }
        val formulesFragment = getFormulesFragment()
        formulesFragment?.let {
            it.loadFormules()
        }
    }

    private fun getPatientListFragment(): PatientListFragment? {
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is PatientListFragment) {
                return fragment
            }
        }
        return null
    }

    private fun getFormulesFragment(): FormulesFragment? {
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is FormulesFragment) {
                return fragment
            }
        }
        return null
    }
}
