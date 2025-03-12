package com.github.bassenecharles.sencare.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import com.github.bassenecharles.sencare.Formule
import com.github.bassenecharles.sencare.R

class FormuleAdapter(private val formuleList: MutableList<Formule>,
                     private val onModifyClick: (Int) -> Unit,
                     private val onDeleteClick: (Int) -> Unit) :
    RecyclerView.Adapter<FormuleAdapter.FormuleViewHolder>() {

    class FormuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.formule_name_text_view)
        val priceTextView: TextView = itemView.findViewById(R.id.formule_price_text_view)
        val descriptionTextView: TextView = itemView.findViewById(R.id.formule_description_text_view)
        val modifyImageView: ImageView = itemView.findViewById(R.id.formule_modify_image_view)
        val deleteImageView: ImageView = itemView.findViewById(R.id.formule_delete_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormuleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.formule_item, parent, false)
        return FormuleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FormuleViewHolder, position: Int) {
        val currentItem = formuleList[position]
        holder.nameTextView.text = currentItem.name
        holder.priceTextView.text = currentItem.price.toString()
        holder.descriptionTextView.text = currentItem.description

        holder.modifyImageView.setOnClickListener {
            onModifyClick(position)
        }

        holder.deleteImageView.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette formule ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    onDeleteClick(position)
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun getItemCount() = formuleList.size
}
