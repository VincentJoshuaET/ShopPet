package com.vt.shoppet.util

import android.app.Activity
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vt.shoppet.R

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

fun Activity.showSnackbar(message: String) =
    Snackbar.make(window.decorView.rootView, message, Snackbar.LENGTH_SHORT).show()

fun Activity.showActionSnackbar(exception: Exception, action: (View) -> Unit) =
    Snackbar.make(window.decorView.rootView, exception.localizedMessage!!, Snackbar.LENGTH_SHORT)
        .setAction(R.string.btn_retry, action).show()

fun Fragment.showSnackbar(message: String) =
    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

fun Fragment.showSnackbar(exception: Exception) =
    Snackbar.make(requireView(), exception.localizedMessage!!, Snackbar.LENGTH_SHORT).show()

fun Fragment.showActionSnackbar(message: String, action: (View) -> Unit) =
    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
        .setAction(R.string.btn_retry, action).show()

fun Fragment.showActionSnackbar(exception: Exception, action: (View) -> Unit) =
    Snackbar.make(requireView(), exception.localizedMessage!!, Snackbar.LENGTH_SHORT)
        .setAction(R.string.btn_retry, action).show()

fun Fragment.setLayout(orientation: Int) = when (orientation) {
    Configuration.ORIENTATION_LANDSCAPE -> GridLayoutManager(context, 3)
    else -> GridLayoutManager(context, 2)
}

fun Button.popBackStackOnClick() = setOnClickListener {
    findNavController().popBackStack()
}

fun RecyclerView.setOnLayoutChangeListener() =
    addOnLayoutChangeListener { _, _, top, _, _, _, oldTop, _, _ ->
        if (top < oldTop) smoothScrollToPosition(oldTop)
    }
