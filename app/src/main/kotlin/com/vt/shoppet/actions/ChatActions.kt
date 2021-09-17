package com.vt.shoppet.actions

import android.view.View
import android.widget.ImageView
import com.vt.shoppet.model.Chat

interface ChatActions {
    fun onClick(chat: Chat): View.OnClickListener
    fun setImage(uid: String, imageView: ImageView)
}