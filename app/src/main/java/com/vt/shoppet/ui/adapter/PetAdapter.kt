package com.vt.shoppet.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.model.Pet
import com.vt.shoppet.ui.holder.PetHolder

class PetAdapter(private val actions: PetActions) : ListAdapter<Pet, PetHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetHolder(parent)

    override fun onBindViewHolder(holder: PetHolder, position: Int) =
        holder.bindView(getItem(position), actions)

    companion object {
        val callback = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean =
                oldItem == newItem
        }
    }

}