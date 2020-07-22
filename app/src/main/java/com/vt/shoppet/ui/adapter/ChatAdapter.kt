package com.vt.shoppet.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.callback.ChatCallback
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.holder.ChatHolder

class ChatAdapter(private val user: User, private val actions: ChatActions) :
    ListAdapter<Chat, ChatHolder>(ChatCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatHolder(parent)

    override fun onBindViewHolder(holder: ChatHolder, position: Int) =
        holder.bindView(getItem(position), user, actions)

}