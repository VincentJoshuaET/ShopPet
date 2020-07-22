package com.vt.shoppet.actions

import android.widget.ImageView

class MessageActions(val action: (id: String, imageView: ImageView) -> Unit) {
    fun setImage(id: String, imageView: ImageView) = action(id, imageView)
}