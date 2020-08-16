package com.vt.shoppet.ui.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.text.trimmedLength
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.Timestamp
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentRegisterBinding
import com.vt.shoppet.model.User
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val binding by viewBinding(FragmentRegisterBinding::bind)

    private val auth: AuthViewModel by viewModels()
    private val firestore: FirestoreViewModel by viewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private val progress by lazy { circularProgress() }
    private val check by lazy { getDrawable(R.drawable.ic_check) }

    private fun verifyEmail() {
        val btnRegister = binding.btnRegister
        progress.start()
        btnRegister.isClickable = false
        btnRegister.icon = progress as Drawable
        auth.verifyEmail().observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                btnRegister.icon = check
                progress.stop()
                binding.snackbar(getString(R.string.txt_verification_sent)).show()
                auth.signOut()
                findNavController().popBackStack()
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    verifyEmail()
                }.show()
                btnRegister.isClickable = true
                btnRegister.icon = check
                progress.stop()
            }
        }
    }

    private fun addUser(user: User) {
        val btnRegister = binding.btnRegister
        progress.start()
        btnRegister.isClickable = false
        btnRegister.icon = progress as Drawable
        firestore.addUser(user).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                verifyEmail()
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    addUser(user)
                }.show()
                btnRegister.isClickable = true
                btnRegister.icon = check
                progress.stop()
            }
        }
    }

    private fun createUser(user: User, email: String, password: String) {
        val btnRegister = binding.btnRegister
        progress.start()
        btnRegister.isClickable = false
        btnRegister.icon = progress as Drawable
        auth.createUser(email, password).observe(viewLifecycleOwner) { result ->
            result.onSuccess { authResult ->
                val uid = authResult.user?.uid ?: return@observe
                addUser(user.copy(uid = uid))
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    createUser(user, email, password)
                }.show()
                btnRegister.isClickable = true
                btnRegister.icon = check
                progress.stop()
            }
        }
    }

    private fun checkUsername(user: User, email: String, password: String) {
        val btnRegister = binding.btnRegister
        progress.start()
        btnRegister.isClickable = false
        btnRegister.icon = progress as Drawable
        firestore.checkUsername(user.username).observe(viewLifecycleOwner) { result ->
            result.onSuccess { snapshot ->
                if (snapshot.isEmpty) createUser(user, email, password)
                else {
                    binding.snackbar(getString(R.string.txt_username_exists)).show()
                    btnRegister.isClickable = true
                    btnRegister.icon = check
                    progress.stop()
                }
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    checkUsername(user, email, password)
                }.show()
                binding.btnRegister.isClickable = true
                btnRegister.icon = check
                progress.stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtName = binding.txtName
        val txtEmail = binding.txtEmail
        val txtUsername = binding.txtUsername
        val txtPassword = binding.txtPassword
        val inputConfirmPassword = binding.inputConfirmPassword
        val txtConfirmPassword = binding.txtConfirmPassword
        val txtMobile = binding.txtMobile
        val txtDateOfBirth = binding.txtDateOfBirth
        val txtProvince = binding.txtProvince
        val txtSex = binding.txtSex
        val btnRegister = binding.btnRegister
        val btnLogin = binding.btnLogin

        val provinceAdapter = getArrayAdapter(resources.getStringArray(R.array.province))
        val sexAdapter = getArrayAdapter(resources.getStringArray(R.array.sex))

        var dateOfBirth =
            LocalDateTime.now().minusYears(18).atZone(localZoneId).toInstant().toEpochMilli()

        txtName.setErrorListener()
        txtEmail.setErrorListener()
        txtUsername.setErrorListener()
        txtPassword.setErrorListener()
        txtConfirmPassword.setErrorListener()
        txtMobile.setErrorListener()
        txtDateOfBirth.setErrorListener()
        txtProvince.setErrorListener()
        txtSex.setErrorListener()

        txtProvince.setAdapter(provinceAdapter)
        txtProvince.setOnClickListener {
            keyboard.hide(this)
        }

        txtSex.setAdapter(sexAdapter)
        txtSex.setOnClickListener {
            keyboard.hide(this)
        }

        btnLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        txtDateOfBirth.setOnClickListener {
            keyboard.hide(this)
            val constraints = setCalendarConstraints(dateOfBirth)
            val builder =
                MaterialDatePicker.Builder.datePicker().apply {
                    setCalendarConstraints(constraints)
                    setSelection(dateOfBirth)
                    setTitleText(R.string.hint_date_of_birth)
                }
            val picker =
                builder.build().apply {
                    addOnPositiveButtonClickListener { long ->
                        dateOfBirth = long
                        val instant = Instant.ofEpochMilli(dateOfBirth)
                        txtDateOfBirth.setText(dateOfBirthFormatter.format(instant))
                    }
                }
            picker.show(parentFragmentManager, picker.toString())
        }

        btnRegister.setOnClickListener {
            keyboard.hide(this)

            val name = txtName.text.toString().capitalizeWords()
            val email = txtEmail.text.toString()
            val username = txtUsername.text.toString()
            val password = txtPassword.text.toString()
            val confirmPassword = txtConfirmPassword.text.toString()
            val mobile = "+63" + txtMobile.text.toString()
            val location = txtProvince.text.toString()
            val sex = txtSex.text.toString()
            val birth = txtDateOfBirth.text.toString()
            var fail = false

            if (name.isEmpty()) {
                txtName.showError(getString(R.string.txt_enter_full_name))
                fail = true
            }
            if (email.isEmpty()) {
                txtEmail.showError(getString(R.string.txt_enter_email))
                fail = true
            }
            if (username.isEmpty() || username.length < 8) {
                txtUsername.showError(getString(R.string.txt_enter_username))
                fail = true
            }
            if (password.isEmpty()) {
                txtPassword.showError(getString(R.string.txt_enter_password))
                fail = true
            }
            if (confirmPassword.isEmpty()) {
                txtConfirmPassword.showError(getString(R.string.txt_confirm_password))
                fail = true
            }
            if (password.trimmedLength() != password.length) {
                txtPassword.showError(getString(R.string.txt_invalid_password))
                fail = true
            }
            if (confirmPassword.trimmedLength() != confirmPassword.length) {
                txtConfirmPassword.showError(getString(R.string.txt_invalid_password))
                fail = true
            }
            if (password.trim() != confirmPassword.trim()) {
                txtConfirmPassword.showError(getString(R.string.txt_passwords_not_match))
                fail = true
            } else {
                inputConfirmPassword.error = null
            }
            if (password.length < 8) {
                txtPassword.showError(getString(R.string.txt_password_short))
                fail = true
            }
            if (mobile.length != 13) {
                txtMobile.showError(getString(R.string.txt_enter_mobile))
                fail = true
            }
            if (location.isEmpty()) {
                txtProvince.showError(getString(R.string.txt_enter_location))
                fail = true
            }
            if (sex.isEmpty()) {
                txtSex.showError(getString(R.string.txt_enter_sex))
                fail = true
            }
            if (birth.isEmpty()) {
                txtDateOfBirth.showError(getString(R.string.txt_enter_date_of_birth))
                fail = true
            }

            if (fail) return@setOnClickListener

            val instant = Instant.ofEpochMilli(dateOfBirth)
            val user = User(
                name = name,
                username = username,
                mobile = mobile,
                location = location,
                sex = sex,
                dateOfBirth = Timestamp(instant.epochSecond, instant.nano)
            )

            checkUsername(user, email, password)
        }
    }

}