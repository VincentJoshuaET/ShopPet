package com.vt.shoppet.ui.auth

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.model.Result
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogoutDialog : DialogFragment() {

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_item_logout)
            .setMessage(R.string.txt_log_out)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                val activity = requireActivity() as MainActivity
                auth.instanceId().observe(activity) { result ->
                    when (result) {
                        is Result.Success -> {
                            firestore.removeToken(result.data.token)
                            auth.signOut()
                            if (auth.isLoggedIn()) showSnackbar(getString(R.string.txt_cannot_log_out))
                            else activity.restartActivity()
                        }
                        is Result.Failure -> showSnackbar(result.exception)
                    }
                }
            }
            .setNegativeButton(R.string.btn_no) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
}