package com.vt.shoppet.ui.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.databinding.ItemPetBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.util.calculateAge
import com.vt.shoppet.util.calculatePostDuration

class PetHolder(binding: ItemPetBinding) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private val imageView = binding.imageView
    private val txtName = binding.txtName
    private val txtPrice = binding.txtPrice
    private val txtType = binding.txtType
    private val txtSex = binding.txtSex
    private val txtAge = binding.txtAge
    private val txtBreed = binding.txtBreed
    private val txtDate = binding.txtDate

    fun bindView(pet: Pet, actions: PetActions) {
        actions.setImage(pet.image, imageView)
        txtName.text = pet.name
        val price = "₱ ${pet.price}"
        txtPrice.text = price
        txtType.text = pet.type
        txtSex.text = pet.sex
        txtAge.text = pet.dateOfBirth.calculateAge
        txtBreed.text = pet.breed
        txtDate.text = pet.date.calculatePostDuration(pet.sold)
        itemView.setOnClickListener(actions.onClick(pet, itemView))
    }

}