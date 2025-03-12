package com.github.bassenecharles.sencare.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import com.github.bassenecharles.sencare.Patient
import com.github.bassenecharles.sencare.R

class PatientAdapter(private val patientList: MutableList<Patient>,
                     private val onModifyClick: (Int) -> Unit,
                     private val onDeleteClick: (Int) -> Unit) :
    RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.patient_name_text_view)
        val prenomTextView: TextView = itemView.findViewById(R.id.patient_prenom_text_view)
        val formuleTextView: TextView = itemView.findViewById(R.id.patient_formule_text_view)
        val modifyImageView: ImageView = itemView.findViewById(R.id.patient_modify_image_view)
        val deleteImageView: ImageView = itemView.findViewById(R.id.patient_delete_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.patient_item, parent, false)
        return PatientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val currentItem = patientList[position]
        holder.nameTextView.text = currentItem.name
        holder.prenomTextView.text = currentItem.prenom
        holder.formuleTextView.text = holder.itemView.context.getString(R.string.detail_patient_formule, currentItem.formule)

        holder.modifyImageView.setOnClickListener {
            onModifyClick(position)
        }

        holder.deleteImageView.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce patient ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    onDeleteClick(position)
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_patient_details, null)

            val nameTextView = dialogView.findViewById<TextView>(R.id.detail_patient_name)
            val prenomTextView = dialogView.findViewById<TextView>(R.id.detail_patient_prenom)
            val dateNaissanceTextView = dialogView.findViewById<TextView>(R.id.detail_patient_date_naissance)
            val telephoneTextView = dialogView.findViewById<TextView>(R.id.detail_patient_telephone)
            val adresseTextView = dialogView.findViewById<TextView>(R.id.detail_patient_adresse)
            val formuleTextViewDialog = dialogView.findViewById<TextView>(R.id.detail_patient_formule)
            val medecinTextView = dialogView.findViewById<TextView>(R.id.detail_patient_medecin)
            val numMedecinTextView = dialogView.findViewById<TextView>(R.id.detail_patient_num_medecin)

            nameTextView.text = holder.itemView.context.getString(R.string.detail_patient_name, currentItem.name)
            prenomTextView.text = holder.itemView.context.getString(R.string.detail_patient_prenom, currentItem.prenom)
            dateNaissanceTextView.text = holder.itemView.context.getString(R.string.detail_patient_date_naissance, currentItem.dateNaissance)
            telephoneTextView.text = holder.itemView.context.getString(R.string.detail_patient_telephone, currentItem.telephone)
            adresseTextView.text = holder.itemView.context.getString(R.string.detail_patient_adresse, currentItem.adresse)
            formuleTextViewDialog.text = holder.itemView.context.getString(R.string.detail_patient_formule, currentItem.formule)
            medecinTextView.text = "Médecin traitant: ${currentItem.medecinTraitant}"
            numMedecinTextView.text = "Numéro médecin: ${currentItem.numMedecin}"

            AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setTitle("Détails du patient")
                .setPositiveButton("Fermer") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}
