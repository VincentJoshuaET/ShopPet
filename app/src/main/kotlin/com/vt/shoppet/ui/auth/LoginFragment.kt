package com.vt.shoppet.ui.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentLoginBinding
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

    private val progress by lazy { circularProgress }

    private fun instanceId() {
        val btnLogin = binding.btnLogin
        progress.start()
        btnLogin.isClickable = false
        btnLogin.icon = progress as Drawable
        auth.getToken().observe(viewLifecycleOwner) { result ->
            result.onSuccess { token ->
                btnLogin.icon = null
                progress.stop()
                firestore.addToken(token)
                dataViewModel.initFirebaseData()
                findNavController().navigate(R.id.action_auth_to_home)
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    instanceId()
                }.show()
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
                val btnLogin = binding.btnLogin
                auth.signOut()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
            .create()

    private fun verifyEmail() {
        val btnLogin = binding.btnLogin
        progress.start()
        btnLogin.isClickable = false
        btnLogin.icon = progress as Drawable
        auth.verifyEmail().observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                binding.snackbar(getString(R.string.txt_verification_sent)).show()
                auth.signOut()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    verifyEmail()
                }.show()
                auth.signOut()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
        }
    }

    private fun unverified(): AlertDialog {
        val btnLogin = binding.btnLogin
        return MaterialAlertDialogBuilder(requireContext())
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
    }

    private fun signIn(email: String, password: String) {
        val btnLogin = binding.btnLogin
        progress.start()
        btnLogin.isClickable = false
        btnLogin.icon = progress as Drawable
        auth.signIn(email, password).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                if (auth.isUserVerified()) disclaimer().show()
                else unverified().show()
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    signIn(email, password)
                }.show()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageLogo = binding.imageLogo
        val txtEmail = binding.txtEmail
        val txtPassword = binding.txtPassword
        val btnForgot = binding.btnForgot
        val btnRegister = binding.btnRegister
        val btnLogin = binding.btnLogin

        txtEmail.setErrorListener()
        txtPassword.setErrorListener()
        val extras = FragmentNavigatorExtras(imageLogo to "logo")

        btnRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginToRegister()
            findNavController().navigate(action, extras)
        }

        btnForgot.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginToForgotPassword()
            findNavController().navigate(action, extras)
        }

        btnLogin.setOnClickListener {
            keyboard.hide()

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