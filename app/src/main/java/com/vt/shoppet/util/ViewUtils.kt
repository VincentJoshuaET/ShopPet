package com.vt.shoppet.util

import android.app.Activity
import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar

fun Fragment.circularProgress(): Animatable {
    val value = TypedValue()
    val context = requireContext()
    context.theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
    val array = intArrayOf(android.R.attr.indeterminateDrawable)
    val attributes = context.obtainStyledAttributes(value.data, array)
    val drawable = attributes.getDrawable(0)
    attributes.recycle()
    return drawable as Animatable
}

fun Fragment.showSnackbar(message: String) =
    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

fun Activity.showSnackbar(message: String) =
    Snackbar.make(window.decorView.rootView, message, Snackbar.LENGTH_SHORT).show()

fun Fragment.showSnackbar(exception: Exception) =
    Snackbar.make(requireView(), exception.localizedMessage!!, Snackbar.LENGTH_SHORT).show()

fun Button.popBackStackOnClick() = setOnClickListener {
    findNavController().popBackStack()
}

fun Button.navigateOnClick(id: Int) = setOnClickListener {
    findNavController().navigate(id)
}