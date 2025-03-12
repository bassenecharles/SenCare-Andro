package com.github.bassenecharles.sencare.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.bassenecharles.sencare.FormuleDao
import com.github.bassenecharles.sencare.PatientDao
import com.github.bassenecharles.sencare.R
import java.math.BigDecimal

class AccueilFragment : Fragment() {

    private lateinit var patientDao: PatientDao
    private lateinit var formuleDao: FormuleDao
    private lateinit var totalPatientsTextView: TextView
    private lateinit var totalGainTextView: TextView
    private lateinit var depensesTextView: TextView
    private lateinit var realGainTextView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        patientDao = PatientDao(context)
        formuleDao = FormuleDao(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_accueil, container, false)

        totalPatientsTextView = view.findViewById(R.id.total_patients_text_view)
        totalGainTextView = view.findViewById(R.id.total_gain_text_view)
        depensesTextView = view.findViewById(R.id.depenses_text_view)
        realGainTextView = view.findViewById(R.id.real_gain_text_view)

        loadTotalPatients()
        loadTotalGain()
        loadDepenses()
        loadRealGain()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadTotalPatients()
        loadTotalGain()
        loadDepenses()
        loadRealGain()
    }

    fun loadTotalPatients() {
        if (::patientDao.isInitialized) {
            patientDao.open()
            val patientCount = patientDao.getAll().size
            patientDao.close()
            totalPatientsTextView.text = "Total Patients: $patientCount"
        }
    }

    fun loadTotalGain() {
        patientDao.open()
        formuleDao.open()
        val patients = patientDao.getAll()
        var totalGain = BigDecimal.ZERO
        for (patient in patients) {
            val formuleName = patient.formule
            val formule = formuleDao.getByName(formuleName)
            if (formule != null) {
                totalGain += formule.price
            }
        }
        patientDao.close()
        formuleDao.close()
        android.util.Log.d("AccueilFragment", "Total Gain: $totalGain")
        totalGainTextView.text = "Total Gain: $totalGain"
    }

    fun loadDepenses() {
        patientDao.open()
        var totalDepenses = BigDecimal.ZERO
        val patients = patientDao.getAll()
        for (patient in patients) {
            totalDepenses += patient.depense
        }
        patientDao.close()
        android.util.Log.d("AccueilFragment", "Total Depenses: $totalDepenses")
        depensesTextView.text = "Dépenses: $totalDepenses"
    }

    fun loadRealGain() {
        patientDao.open()
        formuleDao.open()
        val patients = patientDao.getAll()
        var totalMontantAPayer = BigDecimal.ZERO
        for (patient in patients) {
            patient.montantAPayer?.let {
                totalMontantAPayer += it
            }
        }
        formuleDao.close()
        patientDao.close()

        android.util.Log.d("AccueilFragment", "Real Gain: $totalMontantAPayer")
        realGainTextView.text = "Gains Réels: $totalMontantAPayer"
    }
}
