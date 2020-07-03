package com.vt.shoppet.ui.holder

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.vt.shoppet.R
import com.vt.shoppet.actions.MessageActions
import com.vt.shoppet.databinding.ItemMessageFromBinding
import com.vt.shoppet.databinding.ItemMessageToBinding
import com.vt.shoppet.model.Message
import com.vt.shoppet.util.calculateMessageDate

class MessageHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding =
        when (itemViewType) {
            R.layout.item_message_from -> ItemMessageFromBinding.bind(itemView)
            R.layout.item_message_to -> ItemMessageToBinding.bind(itemView)
            else -> null
        }

    private lateinit var txtMessage: TextView
    private lateinit var txtDate: TextView
    private lateinit var imageMessage: ShapeableImageView

    fun bindView(message: Message, actions: MessageActions) {
        when (binding) {
            is ItemMessageFromBinding -> {
                txtMessage = binding.txtMessage
                txtDate = binding.txtDate
            }
            is ItemMessageToBinding -> {
                txtMessage = binding.txtMessage
                txtDate = binding.txtDate
            }
        }

        txtDate.text = message.date.calculateMessageDate()

        txtMessage.apply {
            isVisible = message.message.isNotEmpty()
            text = message.message
        }

        imageMessage.isVisible = message.image != null
        message.image?.let { image ->
            actions.setImage(image, imageMessage)
        }
    }

}