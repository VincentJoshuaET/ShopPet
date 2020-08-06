package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vt.shoppet.R
import com.vt.shoppet.databinding.DialogSortBinding
import com.vt.shoppet.model.Filter
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SortDialog : BottomSheetDialogFragment() {

    private val viewModel: DataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_sort, container, false)
        val binding = DialogSortBinding.bind(view)

        val rdField = binding.rdField
        val rdAge = binding.rdAge
        val rdBreed = binding.rdBreed
        val rdPrice = binding.rdPrice
        val rdType = binding.rdType
        val rdUploadDate = binding.rdUploadDate

        val rdAscending = binding.rdAscending
        val rdDescending = binding.rdDescending

        val btnOk = binding.btnOk
        val btnCancel = binding.btnCancel
        val btnClear = binding.btnClear

        val savedStateHandle = findNavController().previousBackStackEntry?.savedStateHandle

        lateinit var filter: Filter

        viewModel.filter.observe(this) {
            filter = it
            when (filter.field) {
                "Age" -> rdAge.isChecked = true
                "Breed" -> rdBreed.isChecked = true
                "Price" -> rdPrice.isChecked = true
                "Type" -> rdType.isChecked = true
                "Upload Date" -> rdUploadDate.isChecked = true
            }
            if (filter.order) rdAscending.isChecked = true
            else rdDescending.isChecked = true
        }

        btnCancel.setOnClickListener { findNavController().popBackStack() }

        btnClear.setOnClickListener {
            if (filter.enabled) {
                val update = filter.copy(field = "Upload Date", order = false)
                viewModel.filterPets(update)
                savedStateHandle?.set("filter", true)
            }
            findNavController().popBackStack()
        }

        btnOk.setOnClickListener {
            val field = when (rdField.checkedRadioButtonId) {
                R.id.rdAge -> "Age"
                R.id.rdBreed -> "Breed"
                R.id.rdPrice -> "Price"
                R.id.rdType -> "Type"
                R.id.rdUploadDate -> "Upload Date"
                else -> "Empty"
            }
            val new = filter.copy(
                enabled = true,
                field = field,
                order = rdAscending.isChecked
            )
            viewModel.filterPets(new)
            savedStateHandle?.set("filter", true)
            findNavController().popBackStack()
        }

        return view
    }

}