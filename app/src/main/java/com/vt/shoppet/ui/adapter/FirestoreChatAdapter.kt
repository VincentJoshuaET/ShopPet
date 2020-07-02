package com.vt.shoppet.ui.adapter

import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.holder.ChatHolder

open class FirestoreChatAdapter(
    options: FirestoreRecyclerOptions<Chat>,
    private val user: User
) : FirestoreRecyclerAdapter<Chat, ChatHolder>(options) {

    private lateinit var actions: ChatActions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder =
        ChatHolder(parent)

    override fun onBindViewHolder(holder: ChatHolder, position: Int, model: Chat) =
        holder.bindView(model, user, actions)

    fun setActions(actions: ChatActions) {
        this.actions = actions
    }

}