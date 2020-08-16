package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vt.shoppet.R
import com.vt.shoppet.databinding.DialogFilterBinding
import com.vt.shoppet.model.Filter
import com.vt.shoppet.util.KeyboardUtils
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterDialog : BottomSheetDialogFragment() {

    private val viewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_filter, container, false)
        val binding = DialogFilterBinding.bind(view)

        val chkBird = binding.chkBird
        val chkCat = binding.chkCat
        val chkDog = binding.chkDog
        val chkFish = binding.chkFish
        val chkLizard = binding.chkLizard

        val chkMale = binding.chkMale
        val chkFemale = binding.chkFemale

        val rdPrice = binding.rdPrice
        val rdPriceNoFilter = binding.rdPriceNoFilter
        val rdPriceRange = binding.rdPriceRange
        val sliderPrice = binding.sliderPrice

        val rdAge = binding.rdAge
        val rdAgeNoFilter = binding.rdAgeNoFilter
        val rdAgeDay = binding.rdAgeDay
        val rdAgeWeek = binding.rdAgeWeek
        val rdAgeMonth = binding.rdAgeMonth
        val rdAgeYear = binding.rdAgeYear
        val sliderAge = binding.sliderAge

        val btnOk = binding.btnOk
        val btnCancel = binding.btnCancel
        val btnClear = binding.btnClear

        val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle

        rdPrice.setOnCheckedChangeListener { _, id ->
            sliderPrice.isVisible = when (id) {
                R.id.rdPriceRange -> true
                else -> false
            }
        }

        sliderPrice.setLabelFormatter { value ->
            getString(R.string.sym_currency) + value.toInt()
        }

        rdAge.setOnCheckedChangeListener { _, id ->
            sliderAge.isVisible = when (id) {
                R.id.rdAgeNoFilter -> false
                else -> true
            }
        }

        sliderAge.setLabelFormatter { value ->
            val unit = when (rdAge.checkedRadioButtonId) {
                R.id.rdAgeDay -> "Days"
                R.id.rdAgeWeek -> "Weeks"
                R.id.rdAgeMonth -> "Months"
                R.id.rdAgeYear -> "Years"
                R.id.rdAgeNoFilter -> "No Filter"
                else -> "Empty"
            }
            val age = value.toInt()
            if (age == 1) "$age ${unit.dropLast(1)}"
            else "$age $unit"
        }

        lateinit var filter: Filter

        viewModel.filter.observe(this) {
            filter = it
            chkBird.isChecked = filter.type.contains("Bird")
            chkCat.isChecked = filter.type.contains("Cat")
            chkDog.isChecked = filter.type.contains("Dog")
            chkFish.isChecked = filter.type.contains("Fish")
            chkLizard.isChecked = filter.type.contains("Lizard")

            chkMale.isChecked = filter.sex.contains("Male")
            chkFemale.isChecked = filter.sex.contains("Female")

            if (filter.price) rdPriceRange.isChecked = true
            else rdPriceNoFilter.isChecked = true
            sliderPrice.isVisible = filter.price
            sliderPrice.values = filter.amounts

            when (filter.age) {
                "No Filter" -> rdAgeNoFilter.isChecked = true
                "Days" -> rdAgeDay.isChecked = true
                "Weeks" -> rdAgeWeek.isChecked = true
                "Months" -> rdAgeMonth.isChecked = true
                "Years" -> rdAgeYear.isChecked = true
            }
            sliderAge.isVisible = filter.age != "No Filter"
            sliderAge.values = filter.ages
        }

        btnCancel.setOnClickListener { findNavController().popBackStack() }

        btnClear.setOnClickListener {
            if (filter.enabled) {
                val update = Filter(enabled = true, field = filter.field, order = filter.order)
                viewModel.filterPets(update)
                savedStateHandle?.set("filter", true)
            }
            findNavController().popBackStack()
        }

        btnOk.setOnClickListener {
            val type = mutableListOf<String>()
            if (chkBird.isChecked) type.add("Bird")
            if (chkCat.isChecked) type.add("Cat")
            if (chkDog.isChecked) type.add("Dog")
            if (chkFish.isChecked) type.add("Fish")
            if (chkLizard.isChecked) type.add("Lizard")
            val sex = mutableListOf<String>()
            if (chkMale.isChecked) sex.add("Male")
            if (chkFemale.isChecked) sex.add("Female")
            val age = when (rdAge.checkedRadioButtonId) {
                R.id.rdAgeDay -> "Days"
                R.id.rdAgeWeek -> "Weeks"
                R.id.rdAgeMonth -> "Months"
                R.id.rdAgeYear -> "Years"
                R.id.rdAgeNoFilter -> "No Filter"
                else -> "Empty"
            }
            val new = filter.copy(
                enabled = true,
                type = type,
                sex = sex,
                price = sliderPrice.isVisible,
                amounts = sliderPrice.values,
                age = age,
                ages = sliderAge.values
            )
            viewModel.filterPets(new)
            savedStateHandle?.set("filter", true)
            findNavController().popBackStack()
        }

        return view
    }
}