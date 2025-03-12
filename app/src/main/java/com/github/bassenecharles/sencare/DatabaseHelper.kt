package com.github.bassenecharles.sencare

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Sencare.db"
        private const val DATABASE_VERSION = 5

        // Patient Table
        const val TABLE_PATIENTS = "patients"
        const val COLUMN_PATIENT_ID = "id"
        const val COLUMN_PATIENT_NAME = "name"
        const val COLUMN_PATIENT_PRENOM = "prenom"
        const val COLUMN_PATIENT_DATE_NAISSANCE = "dateNaissance"
        const val COLUMN_PATIENT_TELEPHONE = "telephone"
        const val COLUMN_PATIENT_ADRESSE = "adresse"
        const val COLUMN_PATIENT_FORMULE = "formule"
        const val COLUMN_PATIENT_DEPENSE = "depense"
        const val COLUMN_PATIENT_MONTANT_A_PAYER = "montantAPayer"
        const val COLUMN_PATIENT_MEDECIN_TRAITANT = "medecinTraitant"
        const val COLUMN_PATIENT_NUM_MEDECIN = "numMedecin"

        // Formule Table
        const val TABLE_FORMULES = "formules"
        const val COLUMN_FORMULE_ID = "id"
        const val COLUMN_FORMULE_NAME = "name"
        const val COLUMN_FORMULE_PRICE = "price"
        const val COLUMN_FORMULE_DESCRIPTION = "description"
    }

    private val sqlCreatePatientTable =
        "CREATE TABLE $TABLE_PATIENTS (" +
                "$COLUMN_PATIENT_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_PATIENT_NAME TEXT," +
                "$COLUMN_PATIENT_PRENOM TEXT," +
                "$COLUMN_PATIENT_DATE_NAISSANCE TEXT," +
                "$COLUMN_PATIENT_TELEPHONE TEXT," +
                "$COLUMN_PATIENT_ADRESSE TEXT," +
                "$COLUMN_PATIENT_FORMULE TEXT," +
                "$COLUMN_PATIENT_DEPENSE TEXT," +
                "$COLUMN_PATIENT_MONTANT_A_PAYER TEXT," +
                "$COLUMN_PATIENT_MEDECIN_TRAITANT TEXT," +
                "$COLUMN_PATIENT_NUM_MEDECIN TEXT" +
                ")"

    private val sqlCreateFormuleTable =
        "CREATE TABLE $TABLE_FORMULES (" +
                "$COLUMN_FORMULE_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_FORMULE_NAME TEXT," +
                "$COLUMN_FORMULE_PRICE TEXT," +
                "$COLUMN_FORMULE_DESCRIPTION TEXT" +
                ")"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlCreatePatientTable)
        db.execSQL(sqlCreateFormuleTable)

        addInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        android.util.Log.d("DatabaseHelper", "onUpgrade: oldVersion=$oldVersion, newVersion=$newVersion")
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_PATIENTS ADD COLUMN $COLUMN_PATIENT_DEPENSE TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_PATIENTS ADD COLUMN $COLUMN_PATIENT_MONTANT_A_PAYER TEXT")
        }
        if (oldVersion < 5) {
            android.util.Log.d("DatabaseHelper", "Upgrading database from version $oldVersion to 5")
            db.execSQL("ALTER TABLE $TABLE_PATIENTS ADD COLUMN $COLUMN_PATIENT_MEDECIN_TRAITANT TEXT")
            db.execSQL("ALTER TABLE $TABLE_PATIENTS ADD COLUMN $COLUMN_PATIENT_NUM_MEDECIN TEXT")

            // Update existing rows to populate the new columns
            val values = ContentValues().apply {
                put(COLUMN_PATIENT_MEDECIN_TRAITANT, "")
                put(COLUMN_PATIENT_NUM_MEDECIN, "")
            }
            val rowsUpdated = db.update(TABLE_PATIENTS, values, null, null)
            android.util.Log.d("DatabaseHelper", "Updated $rowsUpdated rows in TABLE_PATIENTS")
        }
    }

    private fun addInitialData(db: SQLiteDatabase) {
    }
}
