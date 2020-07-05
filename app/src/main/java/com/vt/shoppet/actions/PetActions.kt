package com.vt.shoppet.actions

import android.view.View
import android.widget.ImageView
import com.vt.shoppet.model.Pet

interface PetActions {
    fun onClick(pet: Pet, view: View): View.OnClickListener
    fun setImage(id: String, imageView: ImageView)
}