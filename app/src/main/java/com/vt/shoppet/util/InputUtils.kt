package com.vt.shoppet.util

import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout

fun AutoCompleteTextView.setErrorListener() =
    addTextChangedListener {
        (parent.parent as TextInputLayout).error = null
    }

fun EditText.setErrorListener() =
    addTextChangedListener {
        (parent.parent as TextInputLayout).error = null
    }

fun AutoCompleteTextView.showError(message: String) {
    (parent.parent as TextInputLayout).error = message
}

fun EditText.showError(message: String) {
    (parent.parent as TextInputLayout).error = message
}