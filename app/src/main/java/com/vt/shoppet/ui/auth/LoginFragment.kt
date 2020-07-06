package com.vt.shoppet.ui.auth

import android.graphics.Paint
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentLoginBinding
import com.vt.shoppet.model.Result
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val auth: AuthViewModel by activityViewModels()
    private val firestore: FirestoreViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private lateinit var progress: Animatable
    private lateinit var btnLogin: MaterialButton

    private fun instanceId() =
        auth.instanceId().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.start()
                    btnLogin.isClickable = false
                    btnLogin.icon = progress as Drawable
                }
                is Result.Success -> {
                    btnLogin.icon = null
                    progress.stop()
                    firestore.addToken(result.data.token)
                    dataViewModel.initFirebaseData()
                    findNavController().navigate(R.id.action_auth_to_home)
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    auth.signOut()
                    btnLogin.isClickable = true
                    btnLogin.icon = null
                    progress.stop()
                }
            }
        }

    private fun disclaimer(): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.app_name)
            .setMessage(R.string.txt_disclaimer)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_agree) { _, _ ->
                instanceId()
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->
                auth.signOut()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
            .create()

    private fun verifyEmail() =
        auth.verifyEmail().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.start()
                    btnLogin.isClickable = false
                    btnLogin.icon = progress as Drawable
                }
                is Result.Success -> {
                    showSnackbar(getString(R.string.txt_verification_sent))
                    auth.signOut()
                    btnLogin.isClickable = true
                    btnLogin.icon = null
                    progress.stop()
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    auth.signOut()
                    btnLogin.isClickable = true
                    btnLogin.icon = null
                    progress.stop()
                }
            }
        }

    private fun unverified(): AlertDialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_cannot_log_in)
            .setMessage(R.string.txt_account_unverified)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_resend) { _, _ ->
                verifyEmail()
            }
            .setNegativeButton(R.string.btn_no) { _, _ ->
                auth.signOut()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
            .create()

    private fun signIn(email: String, password: String) =
        auth.signIn(email, password).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.start()
                    btnLogin.isClickable = false
                    btnLogin.icon = progress as Drawable
                }
                is Result.Success -> {
                    if (auth.isUserVerified()) disclaimer().show()
                    else unverified().show()
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    btnLogin.isClickable = true
                    btnLogin.icon = null
                    progress.stop()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = circularProgress()
        btnLogin = binding.btnLogin

        val txtEmail = binding.txtEmail
        val txtPassword = binding.txtPassword
        val btnForgot = binding.btnForgot
        val btnRegister = binding.btnRegister

        btnRegister.paintFlags = btnRegister.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        txtEmail.setErrorListener()
        txtPassword.setErrorListener()

        btnRegister.navigateOnClick(R.id.action_login_to_register)
        btnForgot.navigateOnClick(R.id.action_login_to_forgot_password)

        btnLogin.setOnClickListener {
            keyboard.hide(this)

            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            var fail = false

            if (email.isEmpty()) {
                txtEmail.showError(getString(R.string.txt_enter_email))
                fail = true
            }
            if (password.isEmpty()) {
                txtPassword.showError(getString(R.string.txt_enter_password))
                fail = true
            }

            if (fail) return@setOnClickListener

            signIn(email, password)
        }
    }

}