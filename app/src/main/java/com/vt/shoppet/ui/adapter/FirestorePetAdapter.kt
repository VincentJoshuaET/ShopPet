package com.vt.shoppet.ui.adapter

import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.vt.shoppet.model.Pet
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.holder.PetHolder
import com.vt.shoppet.actions.PetActions

open class FirestorePetAdapter(options: FirestoreRecyclerOptions<Pet>) :
    FirestoreRecyclerAdapter<Pet, PetHolder>(options) {

    private lateinit var actions: PetActions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetHolder(parent)

    override fun onBindViewHolder(holder: PetHolder, position: Int, model: Pet) =
        holder.bindView(model, actions)

    fun setActions(actions: PetActions) {
        this.actions = actions
    }

}