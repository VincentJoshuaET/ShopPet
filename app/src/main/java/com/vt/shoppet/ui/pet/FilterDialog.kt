package com.vt.shoppet.ui.pet

import android.app.Dialog
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.DialogFilterBinding
import com.vt.shoppet.model.Filter
import com.vt.shoppet.util.KeyboardUtils
import com.vt.shoppet.util.getArrayAdapter
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterDialog : DialogFragment() {

    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogFilterBinding.inflate(layoutInflater)

        val txtType = binding.txtType
        val txtSex = binding.txtSex
        val txtPrice = binding.txtPrice
        val txtCurrency = binding.txtCurrency
        val sliderPrice = binding.sliderPrice
        val txtAge = binding.txtAge
        val sliderAge = binding.sliderAge

        val typeAdapter = getArrayAdapter(resources.getStringArray(R.array.type_filter))
        val sexAdapter = getArrayAdapter(resources.getStringArray(R.array.sex_filter))
        val priceAdapter = getArrayAdapter(resources.getStringArray(R.array.price_filter))
        val ageAdapter = getArrayAdapter(resources.getStringArray(R.array.age_unit_filter))

        val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle

        txtType.setAdapter(typeAdapter)
        txtSex.setAdapter(sexAdapter)

        txtPrice.setAdapter(priceAdapter)
        txtPrice.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            val range = item == "Range"
            txtCurrency.isVisible = range
            sliderPrice.isVisible = range
        }

        txtAge.setAdapter(ageAdapter)
        txtAge.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            val range = item != "No Filter"
            sliderAge.isVisible = range
        }

        sliderPrice.setLabelFormatter { value ->
            getString(R.string.sym_currency) + value.toInt()
        }

        sliderAge.setLabelFormatter {
            when (val value = it.toInt()) {
                1 -> value.toString() + " " + txtAge.text.toString().dropLast(2)
                else -> value.toString() + " " + txtAge.text.toString().replace("/", "")
            }
        }

        lateinit var filter: Filter

        dataViewModel.filter.observe(this) {
            filter = it
            txtType.setText(filter.type, false)
            txtSex.setText(filter.sex, false)
            txtPrice.setText(filter.price, false)
            sliderPrice.isVisible = filter.price == "Range"
            sliderPrice.values = filter.amounts
            txtAge.setText(filter.age, false)
            sliderAge.isVisible = filter.age != "No Filter"
            sliderAge.values = filter.ages
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_item_filter)
            .setView(binding.root)
            .setNeutralButton(R.string.btn_remove_filters) { _, _ ->
                if (filter.enabled) {
                    val update = Filter(enabled = true, field = filter.field, order = filter.order)
                    dataViewModel.filterPets(update)
                    savedStateHandle?.set("filter", true)
                }
            }
            .setPositiveButton(R.string.btn_ok) { _, _ ->
                val new = filter.copy(
                    enabled = true,
                    type = txtType.text.toString(),
                    sex = txtSex.text.toString(),
                    price = txtPrice.text.toString(),
                    amounts = sliderPrice.values,
                    age = txtAge.text.toString(),
                    ages = sliderAge.values
                )
                dataViewModel.filterPets(new)
                savedStateHandle?.set("filter", true)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
    }
}