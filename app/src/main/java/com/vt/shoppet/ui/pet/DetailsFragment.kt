package com.vt.shoppet.ui.pet

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.Timestamp
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentDetailsBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.FirestoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val binding by viewBinding(FragmentDetailsBinding::bind)
    private val args: DetailsFragmentArgs by navArgs()
    private val firestore: FirestoreViewModel by viewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private val progress by lazy { circularProgress() }
    private val save by lazy { getDrawable(R.drawable.ic_save) }
    private lateinit var toolbar: MaterialToolbar

    private fun updatePet(pet: Pet) {
        progress.start()
        toolbar.menu.getItem(0).icon = progress as Drawable
        firestore.updatePet(pet).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                toolbar.menu.getItem(0).icon = save
                progress.stop()
                findNavController().run {
                    getBackStackEntry(R.id.fragment_shop).savedStateHandle.set("posted", true)
                    popBackStack(R.id.fragment_shop, false)
                }
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    updatePet(pet)
                }.show()
                toolbar.menu.getItem(0).icon = save
                progress.stop()
            }
        }
    }

    private fun addPet(pet: Pet) {
        progress.start()
        toolbar.menu.getItem(0).icon = progress as Drawable
        firestore.addPet(pet).observe(viewLifecycleOwner) { result ->
            result.onSuccess { reference ->
                updatePet(pet.copy(id = reference.id))
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    addPet(pet)
                }.show()
                toolbar.menu.getItem(0).icon = save
                progress.stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        toolbar = activity.toolbar

        val txtName = binding.txtName
        val txtPrice = binding.txtPrice
        val txtType = binding.txtType
        val txtBreed = binding.txtBreed
        val inputCustomBreed = binding.inputCustomBreed
        val txtCustomBreed = binding.txtCustomBreed
        val txtVaccineStatus = binding.txtVaccineStatus
        val txtMedicalRecords = binding.txtMedicalRecords
        val txtSex = binding.txtSex
        val txtAge = binding.txtAge
        val txtUnit = binding.txtUnit
        val layoutCatsDogs = binding.layoutCatsDogs
        val txtDescription = binding.txtDescription

        val types = resources.getStringArray(R.array.type)
        val typeAdapter = getArrayAdapter(types)
        val vaccineStatusAdapter = getArrayAdapter(resources.getStringArray(R.array.vaccine_status))
        val medicalRecordsAdapter =
            getArrayAdapter(resources.getStringArray(R.array.medical_records))
        val sexAdapter = getArrayAdapter(resources.getStringArray(R.array.sex))
        val units = resources.getStringArray(R.array.age_unit)
        val ageAdapter = getArrayAdapter(units)

        val uid = firestore.uid
        val username = args.username
        val image = args.image

        txtName.setErrorListener()
        txtPrice.setErrorListener()
        txtType.setErrorListener()
        txtBreed.setErrorListener()
        txtCustomBreed.setErrorListener()
        txtVaccineStatus.setErrorListener()
        txtMedicalRecords.setErrorListener()
        txtSex.setErrorListener()
        txtAge.setErrorListener()
        txtUnit.setErrorListener()

        txtAge.transformationMethod = null
        txtPrice.transformationMethod = null

        var type = ""
        var custom = false

        fun setBreedDropdown() {
            val array = when (type) {
                "Bird" -> resources.getStringArray(R.array.bird)
                "Cat" -> resources.getStringArray(R.array.cat)
                "Dog" -> resources.getStringArray(R.array.dog)
                "Fish" -> resources.getStringArray(R.array.fish)
                else -> resources.getStringArray(R.array.lizard)
            }
            val adapter = getArrayAdapter(array)
            layoutCatsDogs.isVisible = type == types[1] || type == types[2]
            txtBreed.setAdapter(adapter)
        }

        txtType.setAdapter(typeAdapter)
        txtType.setOnClickListener {
            keyboard.hide(this)
        }
        txtType.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            if (type == item) return@setOnItemClickListener
            type = item
            txtBreed.text = null
            txtCustomBreed.text = null
            inputCustomBreed.isVisible = false
            setBreedDropdown()
        }

        txtBreed.setOnClickListener {
            keyboard.hide(this)
            if (txtBreed.adapter == null) txtType.showError(getString(R.string.txt_select_type))
        }
        txtBreed.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            custom = item == "Other"
            if (custom) inputCustomBreed.isVisible = true
            else {
                inputCustomBreed.isVisible = false
                txtCustomBreed.text = null
            }
        }

        txtVaccineStatus.setAdapter(vaccineStatusAdapter)
        txtVaccineStatus.setOnClickListener {
            keyboard.hide(this)
        }

        txtMedicalRecords.setAdapter(medicalRecordsAdapter)
        txtMedicalRecords.setOnClickListener {
            keyboard.hide(this)
        }

        txtSex.setAdapter(sexAdapter)
        txtSex.setOnClickListener {
            keyboard.hide(this)
        }

        txtUnit.setAdapter(ageAdapter)
        txtUnit.setOnClickListener {
            keyboard.hide(this)
        }


        toolbar.setOnMenuItemClickListener { item ->
            keyboard.hide(this)
            when (item.itemId) {
                R.id.item_save -> {
                    val name = txtName.text.toString().capitalizeWords()
                    val price = txtPrice.text.toString().toIntOrNull() ?: 0
                    val vaccineStatus =
                        txtVaccineStatus.text.toString().run { if (isNotEmpty()) this else null }
                    val medicalRecords =
                        txtMedicalRecords.text.toString().run { if (isNotEmpty()) this else null }
                    val sex = txtSex.text.toString()
                    val age = txtAge.text.toString().toIntOrNull() ?: 0
                    val unit = txtUnit.text.toString()
                    val description = txtDescription.text.toString().capitalize(Locale.getDefault())
                    val breed =
                        if (custom) txtCustomBreed.text.toString().capitalizeWords()
                        else txtBreed.text.toString()
                    var fail = false

                    if (name.isEmpty()) {
                        txtName.showError(getString(R.string.txt_enter_name))
                        fail = true
                    }
                    if (price == 0) {
                        txtPrice.showError(getString(R.string.txt_enter_price))
                        fail = true
                    }
                    if (type.isEmpty()) {
                        txtType.showError(getString(R.string.txt_select_type))
                        fail = true
                    }
                    if (breed.isEmpty()) {
                        if (custom) txtCustomBreed.showError(getString(R.string.txt_enter_breed))
                        else txtBreed.showError(getString(R.string.txt_enter_breed))
                        fail = true
                    }
                    if (type == types[1] || type == types[2]) {
                        if (vaccineStatus == null) {
                            txtVaccineStatus.showError(getString(R.string.txt_enter_vaccine_status))
                            fail = true
                        }
                        if (medicalRecords == null) {
                            txtMedicalRecords.showError(getString(R.string.txt_enter_medical_records))
                            fail = true
                        }
                    }
                    if (sex.isEmpty()) {
                        txtSex.showError(getString(R.string.txt_enter_sex))
                        fail = true
                    }
                    if (age == 0) {
                        txtAge.showError(getString(R.string.txt_enter_age))
                        fail = true
                    }
                    if (unit.isEmpty()) {
                        txtUnit.showError(getString(R.string.txt_enter_age_unit))
                        fail = true
                    }

                    if (fail) return@setOnMenuItemClickListener false

                    val instant = when (unit) {
                        units[0] -> LocalDateTime.now().minusDays(age.toLong()).atZone(localZoneId)
                            .toInstant()
                        units[1] -> LocalDateTime.now().minusWeeks(age.toLong()).atZone(localZoneId)
                            .toInstant()
                        units[2] -> LocalDateTime.now().minusMonths(age.toLong())
                            .atZone(localZoneId)
                            .toInstant()
                        else -> LocalDateTime.now().minusYears(age.toLong()).atZone(localZoneId)
                            .toInstant()
                    }

                    val dateOfBirth = Timestamp(instant.epochSecond, instant.nano)

                    val pet = Pet(
                        name = name,
                        uid = uid,
                        username = username,
                        image = image,
                        type = type,
                        price = price,
                        sex = sex,
                        dateOfBirth = dateOfBirth,
                        breed = breed,
                        vaccineStatus = vaccineStatus,
                        medicalRecords = medicalRecords,
                        description = description,
                        sold = false
                    )

                    addPet(pet)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }

}