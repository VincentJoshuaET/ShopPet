package com.vt.shoppet.ui.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentForgotPasswordBinding
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private val binding by viewBinding(FragmentForgotPasswordBinding::bind)
    private val auth: AuthViewModel by viewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private fun resetPassword(email: String) {
        val progress = circularProgress().apply { start() }
        val btnReset = binding.btnReset
        val icon = getDrawable(R.drawable.ic_email)
        btnReset.isClickable = false
        btnReset.icon = progress as Drawable
        auth.resetPassword(email).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                btnReset.icon = icon
                progress.stop()
                showSnackbar(binding.root, getString(R.string.txt_email_sent))
                findNavController().popBackStack()
            }
            result.onFailure { exception ->
                showActionSnackbar(binding.root, exception) {
                    resetPassword(email)
                }
                btnReset.isClickable = true
                btnReset.icon = icon
                progress.stop()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtEmail = binding.txtEmail
        val btnLogin = binding.btnLogin
        val btnReset = binding.btnReset

        txtEmail.setErrorListener()
        btnLogin.popBackStackOnClick()

        btnReset.setOnClickListener {
            keyboard.hide(this)

            val email = txtEmail.text.toString()

            if (email.isEmpty()) {
                txtEmail.showError(getString(R.string.txt_enter_email))
                return@setOnClickListener
            }

            resetPassword(email)
        }
    }

}