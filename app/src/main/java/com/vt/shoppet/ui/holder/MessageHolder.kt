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

    private lateinit var txtMessage: TextView
    private lateinit var txtDate: TextView
    private lateinit var imageMessage: ShapeableImageView

    fun bindView(message: Message, actions: MessageActions) {
        when (itemViewType) {
            R.layout.item_message_from -> {
                val binding = ItemMessageFromBinding.bind(itemView)
                txtMessage = binding.txtMessage
                txtDate = binding.txtDate
                imageMessage = binding.imageMessage
            }
            R.layout.item_message_to -> {
                val binding = ItemMessageToBinding.bind(itemView)
                txtMessage = binding.txtMessage
                txtDate = binding.txtDate
                imageMessage = binding.imageMessage
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