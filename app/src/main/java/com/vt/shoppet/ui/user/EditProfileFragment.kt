package com.vt.shoppet.ui.user

import android.content.ContentValues
import android.content.pm.PackageManager.FEATURE_CAMERA_ANY
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentEditProfileBinding
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.*
import com.vt.shoppet.util.PermissionUtils.SELECT_PHOTO
import com.vt.shoppet.util.PermissionUtils.TAKE_PHOTO
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val binding by viewBinding(FragmentEditProfileBinding::bind)
    private val viewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var storage: StorageRepo

    private lateinit var imageUser: ShapeableImageView
    private lateinit var imageUserUpload: ShapeableImageView
    private var uri = Uri.EMPTY

    private var action = 0
    private var dateOfBirth = 0L

    private fun checkPermissions() =
        if (checkSelfPermissions()) {
            when (action) {
                SELECT_PHOTO -> selectPhoto.launch("image/*")
                TAKE_PHOTO -> {
                    uri = requireContext().contentResolver.insert(
                        EXTERNAL_CONTENT_URI,
                        ContentValues()
                    )
                    openCamera.launch(uri)
                }
                else -> null
            }
        } else requestPermissions.launch(permissions)

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.checkAllPermissions()) {
                when (action) {
                    SELECT_PHOTO -> selectPhoto.launch("image/*")
                    TAKE_PHOTO -> {
                        uri = requireContext().contentResolver.insert(
                            EXTERNAL_CONTENT_URI,
                            ContentValues()
                        )
                        openCamera.launch(uri)
                    }
                }
            } else showSnackbar(getString(R.string.txt_permission_denied))
        }

    private val selectPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri = it
            loadImage()
        }

    private val openCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { boolean ->
            if (boolean) loadImage()
        }

    private fun loadImage() {
        setProfileImage(imageUserUpload, uri)
        imageUser.isInvisible = true
    }

    private fun clearImageView() {
        imageUser.setImageResource(R.drawable.ic_person)
        imageUserUpload.setImageResource(R.drawable.ic_person)
        imageUser.isVisible = true
        uri = Uri.EMPTY
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val toolbar = activity.toolbar

        imageUser = binding.imageUser
        imageUserUpload = binding.imageUserUpload

        val context = requireContext()
        val circularProgress = circularProgress(context)

        val fabEdit = binding.fabEdit
        val txtName = binding.txtName
        val txtEmail = binding.txtEmail
        val txtUsername = binding.txtUsername
        val txtMobile = binding.txtMobile
        val txtProvince = binding.txtProvince
        val txtSex = binding.txtSex
        val txtDateOfBirth = binding.txtDateOfBirth
        val progress = binding.progress

        val provinces = resources.getStringArray(R.array.province)
        val provinceAdapter = getArrayAdapter(provinces)
        val sexAdapter = getArrayAdapter(resources.getStringArray(R.array.sex))

        val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(zone)
        val max = LocalDateTime.now().minusYears(18).atZone(zone).toInstant().toEpochMilli()

        val removeDialog =
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_remove_image)
                .setMessage(R.string.txt_remove_image)
                .setPositiveButton(R.string.btn_confirm) { _, _ ->
                    firestore.removeUserPhoto().observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Result.Loading -> {
                                circularProgress.start()
                                toolbar.menu.findItem(R.id.item_save).icon =
                                    circularProgress as Drawable
                                fabEdit.isClickable = false
                            }
                            is Result.Success -> {
                                storage.removeUserPhoto()
                                toolbar.menu.findItem(R.id.item_save).setIcon(R.drawable.ic_save)
                                circularProgress.stop()
                                clearImageView()
                                showSnackbar(getString(R.string.txt_profile_updated))
                                findNavController().popBackStack()
                            }
                            is Result.Failure -> {
                                showSnackbar(result.exception)
                                toolbar.menu.findItem(R.id.item_save).setIcon(R.drawable.ic_save)
                                circularProgress.stop()
                                fabEdit.isClickable = true
                            }
                        }
                    }
                }
                .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

        fun editDialog(): AlertDialog {
            val items =
                if (context.packageManager.hasSystemFeature(FEATURE_CAMERA_ANY)) {
                    if (uri == Uri.EMPTY) resources.getStringArray(R.array.add_photo)
                    else resources.getStringArray(R.array.edit_photo)
                } else arrayOf("Upload Photo")
            return MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_profile_image)
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> {
                            action = SELECT_PHOTO
                            checkPermissions()
                        }
                        1 -> {
                            action = TAKE_PHOTO
                            checkPermissions()
                        }
                        2 -> removeDialog.show()
                    }
                }
                .create()
        }

        fun updateUser(user: User) =
            firestore.updateUser(user).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Loading -> {
                        circularProgress.start()
                        toolbar.menu.findItem(R.id.item_save).icon = circularProgress as Drawable
                        fabEdit.isClickable = false
                    }
                    is Result.Success -> {
                        toolbar.menu.findItem(R.id.item_save).setIcon(R.drawable.ic_save)
                        circularProgress.stop()
                        showSnackbar(getString(R.string.txt_profile_updated))
                        findNavController().popBackStack()
                    }
                    is Result.Failure -> {
                        showSnackbar(result.exception)
                        toolbar.menu.findItem(R.id.item_save).setIcon(R.drawable.ic_save)
                        fabEdit.isClickable = true
                        circularProgress.stop()
                    }
                }
            }

        txtName.setErrorListener()
        txtMobile.setErrorListener()
        txtProvince.setErrorListener()
        txtSex.setErrorListener()

        fabEdit.setOnClickListener {
            editDialog().show()
        }

        txtProvince.setAdapter(provinceAdapter)
        txtProvince.setOnClickListener {
            keyboard.hide(this)
        }

        txtSex.setAdapter(sexAdapter)
        txtSex.setOnClickListener {
            keyboard.hide(this)
        }

        viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            val image = user.image
            if (image.isNotEmpty()) {
                loadProfileImage(imageUser, storage.getUserPhoto(image))
            } else imageUser.setImageResource(R.drawable.ic_person)

            txtName.setText(user.name)
            txtEmail.text = auth.email()
            txtUsername.text = user.username
            txtMobile.setText(user.mobile)
            txtSex.setText(user.sex, false)
            txtProvince.setText(user.location, false)
            val zonedDateTime =
                LocalDateTime.ofInstant(user.dateOfBirth.toDate().toInstant(), zone).atZone(zone)
            dateOfBirth = zonedDateTime.toInstant().toEpochMilli()
            txtDateOfBirth.setText(dateTimeFormatter.format(zonedDateTime.toInstant()))

            txtDateOfBirth.setOnClickListener {
                keyboard.hide(this)
                val constraints =
                    CalendarConstraints.Builder().setOpenAt(dateOfBirth).setEnd(max).build()
                val builder = MaterialDatePicker.Builder.datePicker().apply {
                    setCalendarConstraints(constraints)
                    setSelection(dateOfBirth)
                    setTitleText(R.string.hint_date_of_birth)
                }
                val picker = builder.build().apply {
                    addOnPositiveButtonClickListener { long ->
                        dateOfBirth = long
                        val date = dateTimeFormatter.format(Instant.ofEpochMilli(dateOfBirth))
                        txtDateOfBirth.setText(date)
                    }
                }
                picker.show(parentFragmentManager, picker.toString())
            }

            toolbar.setOnMenuItemClickListener { item ->
                keyboard.hide(this)

                when (item.itemId) {
                    R.id.item_save -> {
                        val name = txtName.text.toString()
                        val mobile = txtMobile.text.toString().mobileFormat()
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

                        val instant = Instant.ofEpochMilli(dateOfBirth)
                        val new = user.copy(
                            name = name,
                            sex = sex,
                            dateOfBirth = Timestamp(instant.epochSecond, instant.nano),
                            mobile = mobile,
                            location = location
                        )

                        if (uri != Uri.EMPTY) {
                            val id = UUID.randomUUID().toString()
                            storage.uploadUserPhoto(uri, id).observe(viewLifecycleOwner) { result ->
                                when (result) {
                                    is Result.Loading -> {
                                        progress.isVisible = true
                                        circularProgress.start()
                                        item.icon = circularProgress as Drawable
                                        fabEdit.isClickable = false
                                    }
                                    is Result.Success -> {
                                        progress.isInvisible = true
                                        updateUser(new.copy(image = id))
                                    }
                                    is Result.Failure -> {
                                        showSnackbar(result.exception)
                                        item.setIcon(R.drawable.ic_save)
                                        fabEdit.isClickable = true
                                        progress.isInvisible = true
                                        circularProgress.stop()
                                    }
                                }
                            }
                            return@setOnMenuItemClickListener true
                        } else {
                            updateUser(new)
                            return@setOnMenuItemClickListener true
                        }
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
        }
    }
}