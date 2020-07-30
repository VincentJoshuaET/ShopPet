package com.vt.shoppet.ui.auth

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.showActionSnackbar
import com.vt.shoppet.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class LogoutDialog : DialogFragment() {

    private val auth: AuthViewModel by activityViewModels()

    private fun MainActivity.instanceId() {
        auth.instanceId().observe(this) { result ->
            result.onSuccess { instanceIdResult ->
                signOut(instanceIdResult.token)
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    instanceId()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_item_logout)
            .setMessage(R.string.txt_log_out)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                val activity = requireActivity() as MainActivity
                activity.instanceId()
            }
            .setNegativeButton(R.string.btn_no) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
}