package com.github.bassenecharles.sencare

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.TABLE_PATIENTS
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_ID
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_NAME
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_PRENOM
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_DATE_NAISSANCE
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_TELEPHONE
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_ADRESSE
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_FORMULE
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_DEPENSE
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_MONTANT_A_PAYER
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_MEDECIN_TRAITANT
import com.github.bassenecharles.sencare.DatabaseHelper.Companion.COLUMN_PATIENT_NUM_MEDECIN

class PatientDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private var database: SQLiteDatabase? = null

    fun open() {
        database = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    fun create(patient: Patient): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PATIENT_NAME, patient.name)
            put(DatabaseHelper.COLUMN_PATIENT_PRENOM, patient.prenom)
            put(DatabaseHelper.COLUMN_PATIENT_DATE_NAISSANCE, patient.dateNaissance)
            put(DatabaseHelper.COLUMN_PATIENT_TELEPHONE, patient.telephone)
            put(DatabaseHelper.COLUMN_PATIENT_ADRESSE, patient.adresse)
            put(DatabaseHelper.COLUMN_PATIENT_FORMULE, patient.formule)
            put(DatabaseHelper.COLUMN_PATIENT_DEPENSE, patient.depense.toString())
            put(DatabaseHelper.COLUMN_PATIENT_MONTANT_A_PAYER, patient.montantAPayer?.toString())
            put(DatabaseHelper.COLUMN_PATIENT_MEDECIN_TRAITANT, patient.medecinTraitant)
            put(DatabaseHelper.COLUMN_PATIENT_NUM_MEDECIN, patient.numMedecin)
        }

        val id = database?.insert(TABLE_PATIENTS, null, values) ?: -1

        return id
    }

    fun update(patient: Patient): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PATIENT_NAME, patient.name)
            put(DatabaseHelper.COLUMN_PATIENT_PRENOM, patient.prenom)
            put(DatabaseHelper.COLUMN_PATIENT_DATE_NAISSANCE, patient.dateNaissance)
            put(DatabaseHelper.COLUMN_PATIENT_TELEPHONE, patient.telephone)
            put(DatabaseHelper.COLUMN_PATIENT_ADRESSE, patient.adresse)
            put(DatabaseHelper.COLUMN_PATIENT_FORMULE, patient.formule)
            put(DatabaseHelper.COLUMN_PATIENT_DEPENSE, patient.depense.toString())
            put(DatabaseHelper.COLUMN_PATIENT_MONTANT_A_PAYER, patient.montantAPayer?.toString())
            put(DatabaseHelper.COLUMN_PATIENT_MEDECIN_TRAITANT, patient.medecinTraitant)
            put(DatabaseHelper.COLUMN_PATIENT_NUM_MEDECIN, patient.numMedecin)
        }

        return database?.update(
            TABLE_PATIENTS,
            values,
            "${DatabaseHelper.COLUMN_PATIENT_ID} = ?",
            arrayOf(patient.id.toString())
        ) ?: 0
    }

    fun delete(patient: Patient): Int {
        return database?.delete(
            TABLE_PATIENTS,
            "${DatabaseHelper.COLUMN_PATIENT_ID} = ?",
            arrayOf(patient.id.toString())
        ) ?: 0
    }

    fun getAll(): MutableList<Patient> {
        val patients = mutableListOf<Patient>()

        val cursor: Cursor? = database?.query(
            TABLE_PATIENTS,
            arrayOf(
                COLUMN_PATIENT_ID,
                COLUMN_PATIENT_NAME,
                COLUMN_PATIENT_PRENOM,
                COLUMN_PATIENT_DATE_NAISSANCE,
                COLUMN_PATIENT_TELEPHONE,
                COLUMN_PATIENT_ADRESSE,
                DatabaseHelper.COLUMN_PATIENT_FORMULE,
                COLUMN_PATIENT_DEPENSE,
                COLUMN_PATIENT_MONTANT_A_PAYER,
                COLUMN_PATIENT_MEDECIN_TRAITANT,
                COLUMN_PATIENT_NUM_MEDECIN
            ),
            null,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_PATIENT_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_NAME))
                val prenom = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_PRENOM))
                val dateNaissance = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_DATE_NAISSANCE))
                val telephone = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_TELEPHONE))
                val adresse = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_ADRESSE))
                val formule = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_FORMULE))
                val depenseString = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_DEPENSE))
                val depense = try {
                    java.math.BigDecimal(depenseString).setScale(2, java.math.RoundingMode.HALF_UP)
                } catch (e: NumberFormatException) {
                    java.math.BigDecimal.ZERO
                }
                val montantAPayerString = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_MONTANT_A_PAYER))
                val montantAPayer = try {
                    montantAPayerString?.let { java.math.BigDecimal(it).setScale(2, java.math.RoundingMode.HALF_UP) }
                } catch (e: NumberFormatException) {
                    java.math.BigDecimal.ZERO
                }
                val medecinTraitant = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_MEDECIN_TRAITANT)) ?: ""
                val numMedecin = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_NUM_MEDECIN)) ?: ""

                val patient = Patient(
                    id = id,
                    name = name,
                    prenom = prenom,
                    dateNaissance = dateNaissance,
                    telephone = telephone,
                    adresse = adresse,
                    formule = formule,
                    depense = depense,
                    montantAPayer = montantAPayer,
                    medecinTraitant = medecinTraitant,
                    numMedecin = numMedecin
                )
                patients.add(patient)
            }
        }

        return patients
    }

    fun importPatients(patients: List<Patient>) {
        database?.beginTransaction()
        try {
            patients.forEach { patient ->
                val values = ContentValues().apply {
                    put(COLUMN_PATIENT_NAME, patient.name)
                    put(COLUMN_PATIENT_PRENOM, patient.prenom)
                    put(COLUMN_PATIENT_DATE_NAISSANCE, patient.dateNaissance)
                    put(COLUMN_PATIENT_TELEPHONE, patient.telephone)
                    put(COLUMN_PATIENT_ADRESSE, patient.adresse)
                    put(COLUMN_PATIENT_FORMULE, patient.formule)
                    put(COLUMN_PATIENT_DEPENSE, patient.depense.toString())
                    put(COLUMN_PATIENT_MONTANT_A_PAYER, patient.montantAPayer?.toString())
                    put(COLUMN_PATIENT_MEDECIN_TRAITANT, patient.medecinTraitant)
                    put(COLUMN_PATIENT_NUM_MEDECIN, patient.numMedecin)
                }
                database?.insert(TABLE_PATIENTS, null, values)
            }
            database?.setTransactionSuccessful()
        } finally {
            database?.endTransaction()
        }
    }

    fun getPatientById(id: Long): Patient? {
        val cursor: Cursor? = database?.query(
            TABLE_PATIENTS,
            arrayOf(
                COLUMN_PATIENT_ID,
                COLUMN_PATIENT_NAME,
                COLUMN_PATIENT_PRENOM,
                COLUMN_PATIENT_DATE_NAISSANCE,
                COLUMN_PATIENT_TELEPHONE,
                COLUMN_PATIENT_ADRESSE,
                COLUMN_PATIENT_FORMULE,
                COLUMN_PATIENT_DEPENSE,
                COLUMN_PATIENT_MONTANT_A_PAYER,
                COLUMN_PATIENT_MEDECIN_TRAITANT,
                COLUMN_PATIENT_NUM_MEDECIN
            ),
            "$COLUMN_PATIENT_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_PATIENT_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_NAME))
                val prenom = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_PRENOM))
                val dateNaissance = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATIENT_DATE_NAISSANCE))
                val telephone = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATIENT_TELEPHONE))
                val adresse = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATIENT_ADRESSE))
                val formule = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATIENT_FORMULE))
                val depenseString = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATIENT_DEPENSE))
                val depense = try {
                    java.math.BigDecimal(depenseString).setScale(2, java.math.RoundingMode.HALF_UP)
                } catch (e: NumberFormatException) {
                    java.math.BigDecimal.ZERO
                }
                val montantAPayerString = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_MONTANT_A_PAYER))
                val montantAPayer = try {
                    montantAPayerString?.let { java.math.BigDecimal(it).setScale(2, java.math.RoundingMode.HALF_UP) }
                } catch (e: NumberFormatException) {
                    java.math.BigDecimal.ZERO
                }
                val medecinTraitant = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_MEDECIN_TRAITANT)) ?: ""
                val numMedecin = it.getString(it.getColumnIndexOrThrow(COLUMN_PATIENT_NUM_MEDECIN)) ?: ""

                return Patient(
                    id = id,
                    name = name,
                    prenom = prenom,
                    dateNaissance = dateNaissance,
                    telephone = telephone,
                    adresse = adresse,
                    formule = formule,
                    depense = depense,
                    montantAPayer = montantAPayer,
                    medecinTraitant = medecinTraitant,
                    numMedecin = numMedecin
                )
            }
        }

        return null
    }
}
