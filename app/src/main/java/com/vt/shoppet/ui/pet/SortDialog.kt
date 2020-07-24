package com.vt.shoppet.ui.pet

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.DialogSortBinding
import com.vt.shoppet.util.filter
import com.vt.shoppet.util.getArrayAdapter
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SortDialog : DialogFragment() {

    private val viewModel: DataViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSortBinding.inflate(layoutInflater)
        val txtField = binding.txtField
        val txtOrder = binding.txtOrder

        val fieldAdapter = getArrayAdapter(resources.getStringArray(R.array.field))
        val orderAdapter = getArrayAdapter(resources.getStringArray(R.array.order))

        val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle

        txtField.setAdapter(fieldAdapter)
        txtOrder.setAdapter(orderAdapter)

        viewModel.filter.observe(this) { filter ->
            txtField.setText(filter.field, false)
            txtOrder.setText(filter.order, false)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_item_sort)
            .setView(binding.root)
            .setNeutralButton(R.string.btn_remove_filters) { _, _ ->
                viewModel.filter.observe(this) { filter ->
                    viewModel.pets.observe(this) { pets ->
                        filter.enabled = true
                        filter.field = "Upload Date"
                        filter.order = "Descending"
                        viewModel.setFilteredPets(pets.filter(filter))
                        savedStateHandle?.set("filter", true)
                    }
                }
            }
            .setPositiveButton(R.string.btn_ok) { _, _ ->
                viewModel.filter.observe(this) { filter ->
                    viewModel.pets.observe(this) { pets ->
                        filter.enabled = true
                        filter.field = txtField.text.toString()
                        filter.order = txtOrder.text.toString()
                        viewModel.setFilteredPets(pets.filter(filter))
                        savedStateHandle?.set("filter", true)
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

}