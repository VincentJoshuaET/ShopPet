package com.vt.shoppet.ui.auth

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentForgotPasswordBinding
import com.vt.shoppet.model.Result
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private val binding by viewBinding(FragmentForgotPasswordBinding::bind)
    private val auth: AuthViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private lateinit var progress: Animatable
    private lateinit var btnReset: MaterialButton
    private lateinit var emailIcon: Drawable

    private fun resetPassword(email: String) =
        auth.resetPassword(email).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.start()
                    btnReset.isClickable = false
                    btnReset.icon = progress as Drawable
                }
                is Result.Success -> {
                    btnReset.icon = emailIcon
                    progress.stop()
                    showSnackbar(getString(R.string.txt_email_sent))
                    findNavController().popBackStack()
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    btnReset.isClickable = true
                    btnReset.icon = emailIcon
                    progress.stop()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        progress = circularProgress()
        btnReset = binding.btnReset as MaterialButton
        emailIcon = resources.getDrawable(R.drawable.ic_email, context.theme)

        val txtEmail = binding.txtEmail
        val btnLogin = binding.btnLogin

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