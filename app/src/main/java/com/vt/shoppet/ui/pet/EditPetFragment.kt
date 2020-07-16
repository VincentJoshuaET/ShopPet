package com.vt.shoppet.ui.pet

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentEditPetBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.Result
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditPetFragment : Fragment(R.layout.fragment_edit_pet) {

    private val binding by viewBinding(FragmentEditPetBinding::bind)

    private val firestore: FirestoreViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private lateinit var progress: Animatable
    private lateinit var save: Drawable
    private lateinit var btnSave: MaterialButton

    private fun updatePet(pet: Pet) {
        firestore.updatePet(pet).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.start()
                    btnSave.isClickable = false
                    btnSave.icon = progress as Drawable
                }
                is Result.Success -> {
                    btnSave.icon = save
                    progress.stop()
                    dataViewModel.setCurrentPet(pet)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "edited",
                        true
                    )
                    findNavController().popBackStack()
                }
                is Result.Failure -> {
                    showActionSnackbar(result.exception) {
                        updatePet(pet)
                    }
                    btnSave.isClickable = true
                    btnSave.icon = save
                    progress.stop()
                }
            }
        }
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = circularProgress()
        btnSave = binding.btnSave
        save = getDrawable(R.drawable.ic_save)

        val txtName = binding.txtName
        val txtPrice = binding.txtPrice
        val txtType = binding.txtType
        val txtBreed = binding.txtBreed
        val txtVaccineStatus = binding.txtVaccineStatus
        val txtMedicalRecords = binding.txtMedicalRecords
        val txtSex = binding.txtSex
        val txtAge = binding.txtAge
        val txtUnit = binding.txtUnit
        val layoutCatsDogs = binding.layoutCatsDogs
        val txtDescription = binding.txtDescription


        val types = resources.getStringArray(R.array.type)
        val vaccineStatusAdapter = getArrayAdapter(resources.getStringArray(R.array.vaccine_status))
        val medicalRecordsAdapter =
            getArrayAdapter(resources.getStringArray(R.array.medical_records))

        txtName.setErrorListener()
        txtPrice.setErrorListener()
        txtVaccineStatus.setErrorListener()
        txtMedicalRecords.setErrorListener()
        txtPrice.transformationMethod = null

        txtVaccineStatus.setAdapter(vaccineStatusAdapter)
        txtVaccineStatus.setOnClickListener {
            keyboard.hide(this)
        }

        txtMedicalRecords.setAdapter(medicalRecordsAdapter)
        txtMedicalRecords.setOnClickListener {
            keyboard.hide(this)
        }

        dataViewModel.getCurrentPet().observe(viewLifecycleOwner) { pet ->
            val type = pet.type
            txtName.setText(pet.name)
            txtPrice.setText(pet.price.toString())
            txtType.setText(type)
            txtBreed.setText(pet.breed)
            layoutCatsDogs.isVisible = type == types[1] || pet.type == types[2]
            txtVaccineStatus.setText(pet.vaccineStatus, false)
            txtMedicalRecords.setText(pet.medicalRecords, false)
            txtSex.setText(pet.sex)
            val age = pet.dateOfBirth.calculateEditableAge()
            txtAge.setText(age.first)
            txtUnit.setText(age.second)
            txtDescription.setText(pet.description)

            btnSave.setOnClickListener {
                keyboard.hide(this)

                val name = txtName.text.toString().capitalizeWords()
                val price = txtPrice.text.toString().toIntOrNull() ?: 0
                val vaccineStatus = txtVaccineStatus.text.toString()
                val medicalRecords = txtMedicalRecords.text.toString()
                val description = txtDescription.text.toString().capitalize(Locale.ROOT)
                var fail = false

                if (name.isEmpty()) {
                    txtName.showError(getString(R.string.txt_enter_name))
                    fail = true
                }
                if (price == 0) {
                    txtPrice.showError(getString(R.string.txt_enter_price))
                    fail = true
                }
                if (type == types[1] || type == types[2]) {
                    if (vaccineStatus.isEmpty()) {
                        txtVaccineStatus.showError(getString(R.string.txt_enter_vaccine_status))
                        fail = true
                    }
                    if (medicalRecords.isEmpty()) {
                        txtMedicalRecords.showError(getString(R.string.txt_enter_medical_records))
                        fail = true
                    }
                }

                if (fail) return@setOnClickListener

                val data = pet.copy(
                    name = name,
                    price = price,
                    vaccineStatus = vaccineStatus,
                    medicalRecords = medicalRecords,
                    description = description
                )

                updatePet(data)
            }
        }
    }

}