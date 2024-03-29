package com.vt.shoppet.util

import android.graphics.drawable.Drawable
import android.widget.ArrayAdapter
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.vt.shoppet.R
import java.util.*

val String.capitalizeWords
    get() = split(" ").joinToString(" ") { string ->
        string.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

fun Fragment.getArrayAdapter(array: Array<String>) =
    ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, array)

fun Fragment.getDrawable(@DrawableRes id: Int): Drawable? =
    ContextCompat.getDrawable(requireContext(), id)