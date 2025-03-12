package com.github.bassenecharles.sencare.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.bassenecharles.sencare.DatabaseHelper
import com.github.bassenecharles.sencare.Formule
import com.github.bassenecharles.sencare.FormuleDao
import com.github.bassenecharles.sencare.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.math.BigDecimal

class FormulesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var formuleAdapter: FormuleAdapter
    private val formuleList = mutableListOf<Formule>()
    private lateinit var formuleDao: FormuleDao

    override fun onAttach(context: Context) {
        super.onAttach(context)
        formuleDao = FormuleDao(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_formules, container, false)

        recyclerView = view.findViewById(R.id.formules_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        formuleAdapter = FormuleAdapter(formuleList,
            { position -> showModifyFormuleDialog(position) },
            { position -> deleteFormule(position) })
        recyclerView.adapter = formuleAdapter

        val addFormuleFab: FloatingActionButton = view.findViewById(R.id.add_formules_fab)
        addFormuleFab.setOnClickListener {
            showAddFormuleDialog()
        }

        loadFormules()

        return view
    }

    public fun loadFormules() {
        formuleDao.open()
        formuleList.clear()
        formuleList.addAll(formuleDao.getAll())
        formuleDao.close()
        formuleAdapter.notifyDataSetChanged()
    }

    private fun showAddFormuleDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_formule, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.formule_name_edit_text)
        val priceEditText: EditText = dialogView.findViewById(R.id.formule_price_edit_text)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.formule_description_edit_text)

        val builder = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Ajouter Formule")
            .setPositiveButton("Oui") { dialog, which ->
                val name = nameEditText.text.toString()
                val priceString = priceEditText.text.toString()
                val price = if (priceString.isNotEmpty()) BigDecimal(priceString) else BigDecimal.ZERO
                val description = descriptionEditText.text.toString()

                val newFormule = Formule(name = name, price = price, description = description)
                formuleDao.open()
                formuleDao.create(newFormule)
                formuleDao.close()
                loadFormules()

                // Show confirmation message
                android.widget.Toast.makeText(context, "Formule ajoutée avec succès", android.widget.Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, which ->
                dialog.cancel()
            }
        builder.show()
    }

    private fun showModifyFormuleDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_formule, null)

        val nameEditText: EditText = dialogView.findViewById(R.id.formule_name_edit_text)
        val priceEditText: EditText = dialogView.findViewById(R.id.formule_price_edit_text)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.formule_description_edit_text)

        val formule = formuleList[position]
        nameEditText.setText(formule.name)
        priceEditText.setText(formule.price.toString())
        descriptionEditText.setText(formule.description)

        val builder = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Modifier Formule")
            .setPositiveButton("Modifier") { dialog, which ->
                val name = nameEditText.text.toString()
                val priceString = priceEditText.text.toString()
                val price = if (priceString.isNotEmpty()) BigDecimal(priceString) else BigDecimal.ZERO
                val description = descriptionEditText.text.toString()

                val modifiedFormule = Formule(id = formule.id, name = name, price =  price, description = description)
                formuleDao.open()
                formuleDao.update(modifiedFormule)
                formuleDao.close()
                loadFormules()
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, which ->
                dialog.cancel()
            }
        builder.show()
    }

    private fun deleteFormule(position: Int) {
        val formule = formuleList[position]
        formuleDao.open()
        formuleDao.delete(formule)
        formuleDao.close()
        loadFormules()

        // Show confirmation message
        android.widget.Toast.makeText(context, "Formule supprimée avec succès", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        formuleDao.close()
    }
}
