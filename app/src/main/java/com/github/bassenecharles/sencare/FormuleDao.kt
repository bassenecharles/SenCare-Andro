package com.github.bassenecharles.sencare

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class FormuleDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private var database: SQLiteDatabase? = null

    fun open() {
        database = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    fun create(formule: Formule): Long {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FORMULE_NAME, formule.name)
            put(DatabaseHelper.COLUMN_FORMULE_PRICE, formule.price.toString())
            put(DatabaseHelper.COLUMN_FORMULE_DESCRIPTION, formule.description)
        }

        return database?.insert(DatabaseHelper.TABLE_FORMULES, null, values) ?: -1
    }

    fun update(formule: Formule): Int {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FORMULE_NAME, formule.name)
            put(DatabaseHelper.COLUMN_FORMULE_PRICE, formule.price.toString())
            put(DatabaseHelper.COLUMN_FORMULE_DESCRIPTION, formule.description)
        }

        return database?.update(
            DatabaseHelper.TABLE_FORMULES,
            values,
            "${DatabaseHelper.COLUMN_FORMULE_ID} = ?",
            arrayOf(formule.id.toString())
        ) ?: 0
    }

    fun delete(formule: Formule): Int {
        return database?.delete(
            DatabaseHelper.TABLE_FORMULES,
            "${DatabaseHelper.COLUMN_FORMULE_ID} = ?",
            arrayOf(formule.id.toString())
        ) ?: 0
    }

    fun getAll(): MutableList<Formule> {
        val formules = mutableListOf<Formule>()

        val cursor: Cursor? = database?.query(
            DatabaseHelper.TABLE_FORMULES,
            arrayOf(
                DatabaseHelper.COLUMN_FORMULE_ID,
                DatabaseHelper.COLUMN_FORMULE_NAME,
                DatabaseHelper.COLUMN_FORMULE_PRICE,
                DatabaseHelper.COLUMN_FORMULE_DESCRIPTION
            ),
            null,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_ID))
                val name = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_NAME))
                val priceString = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_PRICE))
                val price = java.math.BigDecimal(priceString).setScale(2, java.math.RoundingMode.HALF_UP)
                val description = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_DESCRIPTION))

                val formule = Formule(id, name, price, description)
                formules.add(formule)
            }
        }

        return formules
    }

    fun getByName(name: String): Formule? {
        val cursor: Cursor? = database?.query(
            DatabaseHelper.TABLE_FORMULES,
            arrayOf(
                DatabaseHelper.COLUMN_FORMULE_ID,
                DatabaseHelper.COLUMN_FORMULE_NAME,
                DatabaseHelper.COLUMN_FORMULE_PRICE,
                DatabaseHelper.COLUMN_FORMULE_DESCRIPTION
            ),
            "${DatabaseHelper.COLUMN_FORMULE_NAME} = ?",
            arrayOf(name),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_ID))
                val name = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_NAME))
                val priceString = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_PRICE))
                val price = java.math.BigDecimal(priceString)
                val description = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMULE_DESCRIPTION))

                return Formule(id, name, price, description)
            }
        }

        return null
    }
}
