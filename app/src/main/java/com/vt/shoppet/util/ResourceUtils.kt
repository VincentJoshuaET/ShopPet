package com.vt.shoppet.util

import android.graphics.drawable.Drawable
import android.widget.ArrayAdapter
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.vt.shoppet.R
import com.vt.shoppet.model.Filter
import com.vt.shoppet.model.Pet
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@ExperimentalStdlibApi
fun String.capitalizeWords() =
    split(" ").joinToString(" ") { it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT) }

fun String.mobileFormat() =
    if (contains("+63")) this else replaceFirst("0", "+63")

fun Fragment.getArrayAdapter(array: Array<String>) =
    ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, array)

fun Fragment.getDrawable(@DrawableRes id: Int): Drawable =
    resources.getDrawable(id, requireContext().theme)

fun List<Pet>.filter(filter: Filter): List<Pet> {
    val typeList =
        if (filter.type == "All") this
        else filter { it.type == filter.type }

    val sexList =
        if (filter.sex == "Both") typeList
        else typeList.filter { it.sex == filter.sex }

    val priceList =
        if (filter.price == "No Filter") sexList
        else sexList.filter { it.price in filter.amounts[0].toInt()..filter.amounts[1].toInt() }

    val now = LocalDateTime.now()

    val fromInstant = when (filter.age) {
        "Days" -> now.minusDays(filter.ages[0].toLong()).atZone(localZoneId)
            .toInstant()
        "Weeks" -> now.minusWeeks(filter.ages[0].toLong()).atZone(localZoneId)
            .toInstant()
        "Months" -> now.minusMonths(filter.ages[0].toLong()).atZone(localZoneId)
            .toInstant()
        "Years" -> now.minusYears(filter.ages[0].toLong()).atZone(localZoneId)
            .toInstant()
        else -> now.atZone(localZoneId).toInstant()
    }

    val toInstant = when (filter.age) {
        "Days" -> now.minusDays(filter.ages[1].toLong()).atZone(localZoneId)
            .toInstant()
        "Weeks" -> now.minusWeeks(filter.ages[1].toLong()).atZone(localZoneId)
            .toInstant()
        "Months" -> now.minusMonths(filter.ages[1].toLong()).atZone(localZoneId)
            .toInstant()
        "Years" -> now.minusYears(filter.ages[1].toLong()).atZone(localZoneId)
            .toInstant()
        else -> now.atZone(localZoneId).toInstant()
    }

    val ageList =
        if (filter.age == "No Filter") priceList
        else priceList.filter { Instant.ofEpochSecond(it.dateOfBirth.seconds) in toInstant..fromInstant }

    return when (filter.order) {
        "Ascending" -> when (filter.field) {
            "Age" -> ageList.sortedByDescending { it.dateOfBirth }
            "Breed" -> ageList.sortedBy { it.breed }
            "Price" -> ageList.sortedBy { it.price }
            "Type" -> ageList.sortedBy { it.type }
            else -> ageList.sortedBy { it.date }
        }
        else -> when (filter.field) {
            "Age" -> ageList.sortedBy { it.dateOfBirth }
            "Breed" -> ageList.sortedByDescending { it.breed }
            "Price" -> ageList.sortedByDescending { it.price }
            "Type" -> ageList.sortedByDescending { it.type }
            else -> ageList.sortedByDescending { it.date }
        }
    }
}