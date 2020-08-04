package com.vt.shoppet.ui.auth

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentLoginBinding
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val auth: AuthViewModel by viewModels()
    private val firestore: FirestoreViewModel by viewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private lateinit var progress: Animatable
    private lateinit var btnLogin: MaterialButton

    private fun instanceId() {
        progress.start()
        btnLogin.isClickable = false
        btnLogin.icon = progress as Drawable
        auth.instanceId().observe(viewLifecycleOwner) { result ->
            result.onSuccess { instanceIdResult ->
                btnLogin.icon = null
                progress.stop()
                firestore.addToken(instanceIdResult.token)
                dataViewModel.initFirebaseData()
                findNavController().navigate(R.id.action_auth_to_home)
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    instanceId()
                }
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

    private fun verifyEmail() {
        progress.start()
        btnLogin.isClickable = false
        btnLogin.icon = progress as Drawable
        auth.verifyEmail().observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar(getString(R.string.txt_verification_sent))
                auth.signOut()
                btnLogin.isClickable = true
                btnLogin.icon = null
                progress.stop()
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    verifyEmail()
                }
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

    private fun signIn(email: String, password: String) {
        progress.start()
        btnLogin.isClickable = false
        btnLogin.icon = progress as Drawable
        auth.signIn(email, password).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                if (auth.isUserVerified()) disclaimer().show()
                else unverified().show()
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    signIn(email, password)
                }
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

        val imageLogo = binding.imageLogo
        val txtEmail = binding.txtEmail
        val txtPassword = binding.txtPassword
        val btnForgot = binding.btnForgot
        val btnRegister = binding.btnRegister

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