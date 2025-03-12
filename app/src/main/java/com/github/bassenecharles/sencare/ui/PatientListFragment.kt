package com.github.bassenecharles.sencare.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.bassenecharles.sencare.DatabaseHelper
import com.github.bassenecharles.sencare.Patient
import com.github.bassenecharles.sencare.PatientDao
import com.github.bassenecharles.sencare.R
import com.github.bassenecharles.sencare.MainActivity
import com.github.bassenecharles.sencare.PatientListListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.Toast

class PatientListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var patientAdapter: PatientAdapter
    private val patientList = mutableListOf<Patient>()
    private val originalPatientList = mutableListOf<Patient>() // Store the original list
    private lateinit var patientDao: PatientDao
    private lateinit var formuleDao: com.github.bassenecharles.sencare.FormuleDao
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        patientDao = PatientDao(context)
        formuleDao = com.github.bassenecharles.sencare.FormuleDao(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_patient_list, container, false)

        recyclerView = view.findViewById(R.id.patient_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        patientAdapter = PatientAdapter(patientList,
            { position -> showModifyPatientDialog(position) },
            { position -> deletePatient(position) })
        recyclerView.adapter = patientAdapter

        val addPatientFab: FloatingActionButton = view.findViewById(R.id.add_patient_fab)
        addPatientFab.setOnClickListener {
            showAddPatientDialog()
        }

        loadPatients()

        return view
    }

    public fun loadPatients() {
        patientDao.open()
        patientList.clear()
        originalPatientList.clear()
        val patients = patientDao.getAll()
        patientList.addAll(patients)
        originalPatientList.addAll(patients) // Store the original list
        patientDao.close()
        patientAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchEditText: EditText = view.findViewById(R.id.search_patient_edit_text)
        val searchButton: Button = view.findViewById(R.id.search_patient_button)

        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString()
            if (searchText.isNotEmpty()) {
                searchPatient(searchText)
            } else {
                // If the search text is empty, restore the original list
                patientList.clear()
                patientList.addAll(originalPatientList)
                patientAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun searchPatient(searchText: String) {
        patientList.clear()
        for (patient in originalPatientList) {
            if (patient.name.contains(searchText, ignoreCase = true) ||
                patient.prenom.contains(searchText, ignoreCase = true) ||
                patient.telephone.contains(searchText, ignoreCase = true) ||
                patient.adresse.contains(searchText, ignoreCase = true)) {
                patientList.add(patient)
            }
        }
        patientAdapter.notifyDataSetChanged()
    }

    private fun showAddPatientDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_patient, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.patient_name_edit_text)
        val prenomEditText: EditText = dialogView.findViewById(R.id.patient_prenom_edit_text)
        val dateNaissanceTextView: TextView = dialogView.findViewById(R.id.patient_date_naissance_text_view)
        val dateNaissanceButton: Button = dialogView.findViewById(R.id.patient_date_naissance_button)
        val telephoneEditText: EditText = dialogView.findViewById(R.id.patient_telephone_edit_text)
        val adresseEditText: EditText = dialogView.findViewById(R.id.patient_adresse_edit_text)
        val formuleSpinner: Spinner = dialogView.findViewById(R.id.patient_formule_spinner)
        val depenseEditText: EditText = dialogView.findViewById(R.id.patient_depense_edit_text)
        val medecinTraitantEditText: EditText = dialogView.findViewById(R.id.editTextMedecinTraitant)
        val numMedecinEditText: EditText = dialogView.findViewById(R.id.editTextNumMedecin)

        // Load formules from database
        formuleDao.open()
        val formules = formuleDao.getAll()
        formuleDao.close()

        // Create an ArrayAdapter using the string array and a default spinner layout
        val formuleNames = formules.map { it.name }.toMutableList()
        formuleNames.add(0, getString(R.string.select_formule))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, formuleNames)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        formuleSpinner.setSelection(0)

        dateNaissanceButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateNaissanceTextView.text = dateFormat.format(selectedDate.time)
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Apply the adapter to the spinner
        formuleSpinner.adapter = adapter
        formuleSpinner.setSelection(0)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Ajouter un patient")
            .setPositiveButton("Ajouter") { dialog, _ ->
                val name = nameEditText.text.toString()
                val prenom = prenomEditText.text.toString()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateNaissance = dateFormat.format(selectedDate.time)
                val telephone = telephoneEditText.text.toString()
                val adresse = adresseEditText.text.toString()
                val formuleName = formuleSpinner.selectedItem.toString()
                val depenseString = depenseEditText.text.toString()
                val medecinTraitant = medecinTraitantEditText.text.toString()
                val numMedecin = numMedecinEditText.text.toString()

                 if (formuleName == getString(R.string.select_formule)) {
                    Toast.makeText(requireContext(), "Veuillez sélectionner une formule", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val depense = if (depenseString.isNotEmpty()) java.math.BigDecimal(depenseString) else java.math.BigDecimal.ZERO

                formuleDao.open()
                val formule = formuleDao.getByName(formuleName)
                formuleDao.close()

                val formulaPrice = formule?.price ?: java.math.BigDecimal.ZERO
                val montantAPayer = formulaPrice - depense

                val newPatient = Patient(name = name, prenom = prenom, dateNaissance = dateNaissance, telephone = telephone, adresse = adresse, formule = formuleName, depense = depense, montantAPayer = montantAPayer, medecinTraitant = medecinTraitant, numMedecin = numMedecin)

                patientDao.open()
                try {
                    patientDao.create(newPatient)
                    loadPatients()

                    // Show confirmation message
                    android.widget.Toast.makeText(context, "Patient ajouté avec succès", android.widget.Toast.LENGTH_SHORT).show()

                    // Update total patients count in AccueilFragment
                    val mainActivity = requireActivity() as PatientListListener
                    mainActivity.updateTotalPatients()

                    dialog.dismiss()
                } finally {
                    patientDao.close()
                }

                // Show confirmation message
                android.widget.Toast.makeText(context, "Patient ajouté avec succès", android.widget.Toast.LENGTH_SHORT).show()

                // Update total patients count in AccueilFragment
                val mainActivity = requireActivity() as PatientListListener
                mainActivity.updateTotalPatients()

                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.cancel()
            }
        builder.show()
    }

    private fun showModifyPatientDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_patient, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.patient_name_edit_text)
        val prenomEditText: EditText = dialogView.findViewById(R.id.patient_prenom_edit_text)
        val dateNaissanceTextView: TextView = dialogView.findViewById(R.id.patient_date_naissance_text_view)
        val dateNaissanceButton: Button = dialogView.findViewById(R.id.patient_date_naissance_button)
        val telephoneEditText: EditText = dialogView.findViewById(R.id.patient_telephone_edit_text)
        val adresseEditText: EditText = dialogView.findViewById(R.id.patient_adresse_edit_text)
        val formuleSpinner: Spinner = dialogView.findViewById(R.id.patient_formule_spinner)
        val depenseEditText: EditText = dialogView.findViewById(R.id.patient_depense_edit_text)
        val medecinTraitantEditText: EditText = dialogView.findViewById(R.id.editTextMedecinTraitant)
        val numMedecinEditText: EditText = dialogView.findViewById(R.id.editTextNumMedecin)

        val patient = patientList[position]
        nameEditText.setText(patient.name)
        prenomEditText.setText(patient.prenom)
        dateNaissanceTextView.text = patient.dateNaissance
        telephoneEditText.setText(patient.telephone)
        adresseEditText.setText(patient.adresse)
        depenseEditText.setText(patient.depense.toString())
        medecinTraitantEditText.setText(patient.medecinTraitant)
        numMedecinEditText.setText(patient.numMedecin)
        dateNaissanceButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateNaissanceTextView.text = dateFormat.format(selectedDate.time)
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Load formules from database
        formuleDao.open()
        val formules = formuleDao.getAll()
        formuleDao.close()

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, formules.map { it.name })

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        formuleSpinner.adapter = adapter

        // Set the position of the spinner to the patient's formule
        val position = formules.indexOfFirst { it.name == patient.formule }
        formuleSpinner.setSelection(position)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Modify Patient")
            .setPositiveButton("Modifier") { dialog, _ ->
                val name = nameEditText.text.toString()
                val prenom = prenomEditText.text.toString()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateNaissance = dateFormat.format(selectedDate.time)
                val telephone = telephoneEditText.text.toString()
                val adresse = adresseEditText.text.toString()
                val formuleName = formuleSpinner.selectedItem.toString()
                val depenseString = depenseEditText.text.toString()
                val medecinTraitant = medecinTraitantEditText.text.toString()
                val numMedecin = numMedecinEditText.text.toString()

                val depense = if (depenseString.isNotEmpty()) java.math.BigDecimal(depenseString) else java.math.BigDecimal.ZERO

                formuleDao.open()
                val formule = formuleDao.getByName(formuleName)
                formuleDao.close()

                val formulaPrice = formule?.price ?: java.math.BigDecimal.ZERO
                val montantAPayer = formulaPrice - depense

                val modifiedPatient = Patient(id = patient.id, name = name, prenom = prenom, dateNaissance = dateNaissance, telephone = telephone, adresse = adresse, formule = formuleName, depense = depense, montantAPayer = montantAPayer, medecinTraitant = medecinTraitant, numMedecin = numMedecin)
                patientDao.open()
                patientDao.update(modifiedPatient)
                patientDao.close()
                loadPatients()

                // Update total patients count in AccueilFragment
                val mainActivity = requireActivity() as PatientListListener
                mainActivity.updateTotalPatients()

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        builder.show()
    }

    private fun deletePatient(position: Int) {
        val patient = patientList[position]
        patientDao.open()
        patientDao.delete(patient)
        patientDao.close()
        loadPatients()

        // Show confirmation message
        android.widget.Toast.makeText(context, "Patient supprimé avec succès", android.widget.Toast.LENGTH_SHORT).show()
    }
}
