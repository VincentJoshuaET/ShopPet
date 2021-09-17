package com.vt.shoppet.ui.user

import android.Manifest
import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentEditProfileBinding
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val binding by viewBinding(FragmentEditProfileBinding::bind)

    private val auth: AuthViewModel by viewModels()
    private val storage: StorageViewModel by viewModels()
    private val firestore: FirestoreViewModel by viewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private val progress by lazy { circularProgress }
    private val save by lazy { getDrawable(R.drawable.ic_save) }
    private lateinit var toolbar: MaterialToolbar

    private var uri = Uri.EMPTY
    private var image: String? = null

    private var dateOfBirth = 0L

    private fun takePhoto() {
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            val file = File(requireContext().cacheDir, "${System.currentTimeMillis()}.jpg")
            uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            openCamera.launch(uri)
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) takePhoto()
            else binding.snackbar(
                message = getString(R.string.txt_permission_denied),
                owner = viewLifecycleOwner
            ).show()
        }

    private val selectPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                this.uri = uri
                loadImage()
            }
        }

    private val openCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { boolean ->
            if (boolean) loadImage()
        }

    private fun loadImage() = binding.apply {
        setProfileImage(imageUserUpload, uri)
        imageUser.isInvisible = true
    }

    private fun clearImageView() = binding.apply {
        imageUser.setImageResource(R.drawable.ic_person)
        imageUserUpload.setImageResource(R.drawable.ic_person)
        imageUser.isVisible = true
        uri = Uri.EMPTY
        image = null
    }

    private fun removeDialog(id: String): AlertDialog {
        val fabEdit = binding.fabEdit
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_remove_image)
            .setMessage(R.string.txt_remove_image)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                progress.start()
                toolbar.menu.getItem(0).icon = progress as Drawable
                fabEdit.isClickable = false
                firestore.removeUserPhoto().observe(viewLifecycleOwner) { result ->
                    result.onSuccess {
                        storage.removeUserPhoto(id)
                        toolbar.menu.getItem(0).icon = save
                        progress.stop()
                        clearImageView()
                        binding.snackbar(getString(R.string.txt_profile_updated)).show()
                        findNavController().popBackStack()
                    }
                    result.onFailure { exception ->
                        binding.snackbar(
                            message = exception.localizedMessage,
                            owner = viewLifecycleOwner
                        ) {
                            removeDialog(id).show()
                        }.show()
                        toolbar.menu.getItem(0).icon = save
                        progress.stop()
                        fabEdit.isClickable = true
                    }
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
    }

    private fun editDialog(): AlertDialog {
        val context = requireContext()
        val items =
            if (context.packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)) {
                if (image != null || uri != Uri.EMPTY) resources.getStringArray(R.array.edit_photo)
                else resources.getStringArray(R.array.add_photo)
            } else {
                arrayOf("Upload Photo")
            }
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_profile_image)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> selectPhoto.launch("image/*")
                    1 -> takePhoto()
                    2 -> {
                        val image = image
                        if (image != null) removeDialog(image).show()
                        else clearImageView()
                    }
                }
            }
            .create()
    }

    private fun updateUser(user: User) {
        val fabEdit = binding.fabEdit
        progress.start()
        toolbar.menu.getItem(0).icon = progress as Drawable
        fabEdit.isClickable = false
        firestore.updateUser(user).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                toolbar.menu.getItem(0).icon = save
                progress.stop()
                binding.snackbar(getString(R.string.txt_profile_updated)).show()
                findNavController().popBackStack()
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    updateUser(user)
                }.show()
                toolbar.menu.getItem(0).icon = save
                fabEdit.isClickable = true
                progress.stop()
            }
        }
    }

    private fun uploadUserPhoto(user: User, id: String) {
        val fabEdit = binding.fabEdit
        val progressIndicator = binding.progress
        progressIndicator.isVisible = true
        progress.start()
        toolbar.menu.getItem(0).icon = progress as Drawable
        fabEdit.isClickable = false
        storage.uploadUserPhoto(id, uri).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                progressIndicator.isInvisible = true
                updateUser(user.copy(image = id))
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    uploadUserPhoto(user, id)
                }.show()
                toolbar.menu.getItem(0).icon = save
                fabEdit.isClickable = true
                progressIndicator.isInvisible = true
                progress.stop()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        toolbar = activity.toolbar
        val imageUser = binding.imageUser
        val fabEdit = binding.fabEdit

        val txtName = binding.txtName
        val txtEmail = binding.txtEmail
        val txtUsername = binding.txtUsername
        val txtMobile = binding.txtMobile
        val txtProvince = binding.txtProvince
        val txtSex = binding.txtSex
        val txtDateOfBirth = binding.txtDateOfBirth

        val provinces = resources.getStringArray(R.array.province)
        val provinceAdapter = getArrayAdapter(provinces)
        val sexAdapter = getArrayAdapter(resources.getStringArray(R.array.sex))

        txtName.setErrorListener()
        txtMobile.setErrorListener()
        txtProvince.setErrorListener()
        txtSex.setErrorListener()

        fabEdit.setOnClickListener {
            editDialog().show()
        }

        txtProvince.setAdapter(provinceAdapter)
        txtProvince.setOnClickListener {
            keyboard.hide()
        }

        txtSex.setAdapter(sexAdapter)
        txtSex.setOnClickListener {
            keyboard.hide()
        }

        dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            image = user.image
            image?.let { id ->
                loadProfileImage(imageUser, storage.getUserPhoto(id))
            } ?: imageUser.setImageResource(R.drawable.ic_person)

            txtName.setText(user.name)
            txtEmail.text = auth.email()
            txtUsername.text = user.username
            txtMobile.setText(user.mobile.replaceFirst("+63", ""))
            txtSex.setText(user.sex, false)
            txtProvince.setText(user.location, false)
            val instant = Instant.ofEpochSecond(user.dateOfBirth.seconds)
            val dateTime = LocalDateTime.ofInstant(instant, localZoneId)
            dateOfBirth = instant.toEpochMilli()
            txtDateOfBirth.setText(dateOfBirthFormatter.format(dateTime))

            txtDateOfBirth.setOnClickListener {
                keyboard.hide()
                val builder = MaterialDatePicker.Builder.datePicker().apply {
                    setCalendarConstraints(dateOfBirth.calendarConstraints)
                    setSelection(dateOfBirth)
                    setTitleText(R.string.hint_date_of_birth)
                }
                val picker = builder.build().apply {
                    addOnPositiveButtonClickListener { long ->
                        dateOfBirth = long
                        val date = dateOfBirthFormatter.format(Instant.ofEpochMilli(dateOfBirth))
                        txtDateOfBirth.setText(date)
                    }
                }
                picker.show(parentFragmentManager, picker.toString())
            }

            toolbar.setOnMenuItemClickListener { item ->
                keyboard.hide()

                when (item.itemId) {
                    R.id.item_save -> {
                        val name = txtName.text.toString()
                        val mobile = "+63" + txtMobile.text.toString()
                        val sex = txtSex.text.toString()
                        val location = txtProvince.text.toString()
                        var fail = false

                        if (name.isEmpty()) {
                            txtName.showError(getString(R.string.txt_enter_name))
                            fail = true
                        }
                        if (mobile.length != 13) {
                            txtMobile.showError(getString(R.string.txt_enter_mobile))
                            fail = true
                        }
                        if (location.isEmpty() || !provinces.contains(location)) {
                            txtProvince.showError(getString(R.string.txt_enter_location))
                            fail = true
                        }

                        if (fail) return@setOnMenuItemClickListener false

                        val data = user.copy(
                            name = name,
                            sex = sex,
                            dateOfBirth = Timestamp(
                                Instant.ofEpochMilli(dateOfBirth).epochSecond,
                                Instant.ofEpochMilli(dateOfBirth).nano
                            ),
                            mobile = mobile,
                            location = location
                        )

                        if (uri != Uri.EMPTY) {
                            val id = UUID.randomUUID().toString()
                            uploadUserPhoto(data, id)
                            return@setOnMenuItemClickListener true
                        } else {
                            updateUser(data)
                            return@setOnMenuItemClickListener true
                        }
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
        }
    }
}