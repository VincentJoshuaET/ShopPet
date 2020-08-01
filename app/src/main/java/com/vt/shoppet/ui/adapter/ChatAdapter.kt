package com.vt.shoppet.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.holder.ChatHolder

class ChatAdapter(private val user: User, private val actions: ChatActions) :
    ListAdapter<Chat, ChatHolder>(callback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatHolder(parent)

    override fun onBindViewHolder(holder: ChatHolder, position: Int) =
        holder.bindView(getItem(position), user, actions)

    companion object {
        val callback = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean =
                oldItem == newItem
        }
    }

}