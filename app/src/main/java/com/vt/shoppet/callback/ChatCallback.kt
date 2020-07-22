package com.vt.shoppet.callback

import androidx.recyclerview.widget.DiffUtil
import com.vt.shoppet.model.Chat

object ChatCallback : DiffUtil.ItemCallback<Chat>() {
    override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean =
        oldItem == newItem
}