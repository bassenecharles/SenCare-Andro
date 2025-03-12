package com.github.bassenecharles.sencare.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.bassenecharles.sencare.DatabaseHelper
import com.github.bassenecharles.sencare.FormuleDao
import com.github.bassenecharles.sencare.PatientDao
import com.github.bassenecharles.sencare.R
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import com.github.bassenecharles.sencare.Formule
import com.github.bassenecharles.sencare.Patient
import java.text.SimpleDateFormat
import java.util.Locale
import com.github.bassenecharles.sencare.DataImportListener
import com.github.bassenecharles.sencare.MainActivity
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ParametresFragment : Fragment() {

    private lateinit var patientDao: PatientDao
    private lateinit var formuleDao: FormuleDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parametres as Int, container, false)

        patientDao = PatientDao(requireContext())
        formuleDao = FormuleDao(requireContext())

        val buttonImport = view.findViewById<Button>(R.id.button_import)
        val buttonExport = view.findViewById<Button>(R.id.button_export)

        buttonExport.setOnClickListener {
            exportDatabaseToJson(it)
        }

        buttonImport.setOnClickListener {
            importDatabaseFromJson()
        }

        return view
    }

    private fun importDatabaseFromJson() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }

        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    @Serializable
    data class PatientJson(
        val id: Long,
        val name: String,
        val prenom: String,
        val dateNaissance: String,
        val telephone: String,
        val adresse: String,
        val formule: FormuleJson,
        val depense: String,
        val montantAPayer: String?,
        val medecinTraitant: String?,
        val numMedecin: String?
    )

    @Serializable
    data class ExportData(
        val patients: List<PatientJson>,
        val formules: List<FormuleJson>
    )

    @Serializable
    data class FormuleJson(
        val id: Long,
        val name: String,
        val price: String,
        val description: String
    )

    private fun exportDatabaseToJson(view: View) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "sencare_export.json")
        }

        startActivityForResult(intent, EXPORT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EXPORT_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        patientDao.open()
                        formuleDao.open()

                        val patients = patientDao.getAll()
                        val formules = formuleDao.getAll()

                        val patientJsonList = patients.map { patient ->
                            val formule = formuleDao.getByName(patient.formule) ?: Formule(0, "N/A", BigDecimal.ZERO, "N/A")
                            val formuleJson = FormuleJson(formule.id, formule.name, String.format("%.2f", formule.price), formule.description)
                            PatientJson(
                                patient.id,
                                patient.name,
                                patient.prenom,
                                patient.dateNaissance,
                                patient.telephone,
                                patient.adresse,
                                formuleJson,
                                String.format(Locale.US, "%.2f", patient.depense),
                                patient.montantAPayer?.let { String.format(Locale.US, "%.2f", it) },
                                patient.medecinTraitant,
                                patient.numMedecin
                            )
                        }

                        val formuleJsonList = formules.map { formule ->
                            FormuleJson(formule.id, formule.name, String.format(Locale.US, "%.2f", formule.price), formule.description)
                        }.toList()

                        formuleDao.close()
                        patientDao.close()

                        val exportData = ExportData(patientJsonList, formuleJsonList)
                        val jsonString = Json.encodeToString(exportData)

                        outputStream.write(jsonString.toByteArray())
                        Toast.makeText(requireContext(), "Export successful!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    patientDao.close()
                    formuleDao.close()
                }
            }
        } else if (requestCode == IMPORT_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.also { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.use {
                        val jsonString = it.reader().readText()
                        val exportData = try {
                            Json.decodeFromString<ExportData>(jsonString)
                        } catch (e: SerializationException) {
                            Toast.makeText(requireContext(), "Import failed: Invalid JSON format", Toast.LENGTH_SHORT).show()
                            return
                        }

                        patientDao.open()
                        formuleDao.open()

                        val numberFormat = NumberFormat.getInstance(Locale.US)

                        // Import Formulas
                        exportData.formules.forEach { formuleJson ->
                            val price = try {
                                numberFormat.parse(formuleJson.price).toDouble()
                            } catch (e: ParseException) {
                                0.0
                            }
                            val newFormule = Formule(
                                0, // Auto-generated ID
                                formuleJson.name,
                                BigDecimal(price),
                                formuleJson.description
                            )
                            formuleDao.create(newFormule)
                        }

                        // Import Patients
                        exportData.patients.forEach { patientJson ->

                            val depense = try {
                                numberFormat.parse(patientJson.depense).toDouble()
                            } catch (e: ParseException) {
                                0.0
                            }

                            val montantAPayer = try {
                                patientJson.montantAPayer?.let { numberFormat.parse(it).toDouble() } ?: 0.0
                            } catch (e: ParseException) {
                                0.0
                            }

                            val newPatient = Patient(
                                0, // Auto-generated ID
                                patientJson.name,
                                patientJson.prenom,
                                patientJson.dateNaissance,
                                patientJson.telephone,
                                patientJson.adresse,
                                patientJson.formule.name,
                                BigDecimal(depense),
                                BigDecimal(montantAPayer),
                                patientJson.medecinTraitant ?: "",
                                patientJson.numMedecin ?: ""
                            )
                            patientDao.create(newPatient)
                        }

                        formuleDao.close()
                        patientDao.close()

                        Toast.makeText(requireContext(), "Import successful!", Toast.LENGTH_SHORT).show()
                        (requireActivity() as? DataImportListener)?.onDataImported(requireActivity() as MainActivity)
                    }
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } catch (e: SerializationException) {
                    Toast.makeText(requireContext(), "Import failed: Invalid JSON format", Toast.LENGTH_SHORT).show()
                } finally {
                    patientDao.close()
                    formuleDao.close()
                }
            }
        }
    }

    companion object {
        private const val EXPORT_REQUEST_CODE = 123
        private const val IMPORT_REQUEST_CODE = 456
    }
}
