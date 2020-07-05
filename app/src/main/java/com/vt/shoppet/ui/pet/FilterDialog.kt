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
import com.vt.shoppet.util.KeyboardUtils
import com.vt.shoppet.util.getArrayAdapter
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterDialog : DialogFragment() {

    private val viewModel: DataViewModel by activityViewModels()

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
            when (val value = it.toInt().toString()) {
                "1" -> value + " " + txtAge.text.toString().dropLast(2)
                else -> value + " " + txtAge.text.toString().replace("/", "")
            }
        }

        viewModel.getFilter().observe(this) { filter ->
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
                viewModel.getFilter().observe(this) { filter ->
                    filter.enabled = true
                    filter.type = "All"
                    filter.sex = "Both"
                    filter.price = "No Filter"
                    filter.amounts = listOf(0F, 10000F)
                    filter.age = "No Filter"
                    filter.ages = listOf(0F, 100F)
                    viewModel.filterPets()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "filter",
                        true
                    )
                }
            }
            .setPositiveButton(R.string.btn_ok) { _, _ ->
                viewModel.getFilter().observe(this) { filter ->
                    filter.enabled = true
                    filter.type = txtType.text.toString()
                    filter.sex = txtSex.text.toString()
                    filter.price = txtPrice.text.toString()
                    filter.amounts = sliderPrice.values
                    filter.age = txtAge.text.toString()
                    filter.ages = sliderAge.values
                    viewModel.filterPets()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "filter",
                        true
                    )
                }
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}