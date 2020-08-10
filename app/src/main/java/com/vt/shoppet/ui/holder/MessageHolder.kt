package com.vt.shoppet.ui.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.vt.shoppet.R
import com.vt.shoppet.actions.MessageActions
import com.vt.shoppet.databinding.ItemMessageFromBinding
import com.vt.shoppet.databinding.ItemMessageToBinding
import com.vt.shoppet.model.Message
import com.vt.shoppet.util.calculateMessageDate

class MessageHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup, viewType: Int) : this(
        if (viewType == R.layout.item_message_from) ItemMessageFromBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
        else ItemMessageToBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    private val txtMessage = when (binding) {
        is ItemMessageFromBinding -> binding.txtMessage
        is ItemMessageToBinding -> binding.txtMessage
        else -> null
    }

    private val txtDate = when (binding) {
        is ItemMessageFromBinding -> binding.txtDate
        is ItemMessageToBinding -> binding.txtDate
        else -> null
    }

    private val imageMessage = when (binding) {
        is ItemMessageFromBinding -> binding.imageMessage
        is ItemMessageToBinding -> binding.imageMessage
        else -> null
    }

    fun bindView(message: Message, actions: MessageActions) {
        txtDate?.text = message.date.calculateMessageDate()
        txtMessage?.isVisible = message.message.isNotEmpty()
        txtMessage?.text = message.message
        if (imageMessage != null) {
            imageMessage.isVisible = message.image != null
            val image = message.image ?: return
            actions.setImage(image, imageMessage)
        }
    }

}