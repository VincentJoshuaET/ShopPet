package com.vt.shoppet.util

import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.vt.shoppet.R
import java.util.*

@ExperimentalStdlibApi
fun String.capitalizeWords() =
    split(" ").joinToString(" ") { it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT) }

fun String.mobileFormat() =
    if (contains("+63")) this else replaceFirst("0", "+63")

fun Fragment.getArrayAdapter(array: Array<String>) =
    ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, array)