package com.vt.shoppet.util

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception

fun circularProgress(context: Context): Animatable {
    val value = TypedValue()
    context.theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
    val array = intArrayOf(android.R.attr.indeterminateDrawable)
    val attributes = context.obtainStyledAttributes(value.data, array)
    val drawable = attributes.getDrawable(0)
    attributes.recycle()
    return drawable as Animatable
}

fun Fragment.showSnackbar(message: String) =
    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

fun Fragment.showSnackbar(exception: Exception) =
    exception.localizedMessage?.let { Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT) }?.show()

fun Button.popBackStackOnClick() = setOnClickListener {
    findNavController().popBackStack()
}

fun Button.navigateOnClick(id: Int) = setOnClickListener {
    findNavController().navigate(id)
}