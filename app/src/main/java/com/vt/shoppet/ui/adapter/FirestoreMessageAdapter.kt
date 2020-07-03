package com.vt.shoppet.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.vt.shoppet.actions.MessageActions
import com.vt.shoppet.model.Message
import com.vt.shoppet.ui.holder.MessageHolder

open class FirestoreMessageAdapter(options: FirestoreRecyclerOptions<Message>) :
    FirestoreRecyclerAdapter<Message, MessageHolder>(options) {

    private lateinit var actions: MessageActions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder =
        MessageHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))

    override fun onBindViewHolder(holder: MessageHolder, position: Int, model: Message) =
        holder.bindView(model, actions)

    fun setActions(actions: MessageActions) {
        this.actions = actions
    }
}