package com.vt.shoppet.callback

import androidx.recyclerview.widget.DiffUtil
import com.vt.shoppet.model.Pet

object PetCallback : DiffUtil.ItemCallback<Pet>() {
    override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean =
        oldItem == newItem
}