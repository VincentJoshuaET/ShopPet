package com.vt.shoppet.ui.holder

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.databinding.ItemChatBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.User
import com.vt.shoppet.util.chatDate

class ChatHolder(binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private val imageUser = binding.imageUser
    private val txtUsername = binding.txtUsername
    private val txtMessage = binding.txtMessage
    private val txtDate = binding.txtDate

    fun bindView(
        chat: Chat,
        user: User,
        actions: ChatActions
    ) {
        when (user.uid) {
            chat.uid[0] -> {
                val username = chat.username[1]
                val unread = !chat.read[0]
                val uid = chat.uid[1]
                txtUsername.text = username
                txtMessage.typeface = if (unread) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                actions.setImage(uid, imageUser)
            }
            chat.uid[1] -> {
                val username = chat.username[0]
                val unread = !chat.read[1]
                val uid = chat.uid[0]
                txtUsername.text = username
                txtMessage.typeface = if (unread) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                actions.setImage(uid, imageUser)
            }
        }

        txtMessage.text = chat.message
        txtDate.text = chat.date.chatDate

        itemView.setOnClickListener(actions.onClick(chat))
    }

}