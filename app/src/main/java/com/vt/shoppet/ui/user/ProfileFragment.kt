package com.vt.shoppet.ui.user

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentProfileBinding
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val args: ProfileFragmentArgs by navArgs()

    private val auth: AuthViewModel by activityViewModels()
    private val firestore: FirestoreViewModel by activityViewModels()
    private val storage: StorageViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val toolbar = activity.toolbar

        val context = requireContext()
        val progress = circularProgress()

        val currentUid = auth.uid()
        val email = auth.email()

        val imageUser = binding.imageUser
        val txtName = binding.txtName
        val txtEmailTitle = binding.txtEmailTitle
        val txtEmail = binding.txtEmail
        val txtUsername = binding.txtUsername
        val txtMobile = binding.txtMobile
        val txtLocation = binding.txtLocation
        val txtSex = binding.txtSex
        val txtDateOfBirthTitle = binding.txtDateOfBirthTitle
        val txtDateOfBirth = binding.txtDateOfBirth

        val report = resources.getDrawable(R.drawable.ic_report, context.theme)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

        fun reportUser(uid: String, currentUid: String) =
            firestore.reportUser(uid, currentUid).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Loading -> {
                        progress.start()
                        toolbar.menu.getItem(0).icon = progress as Drawable
                    }
                    is Result.Success -> {
                        toolbar.menu.getItem(0).icon = report
                        progress.stop()
                        showSnackbar(getString(R.string.txt_reported_user))
                    }
                    is Result.Failure -> {
                        showSnackbar(result.exception)
                        toolbar.menu.getItem(0).icon = report
                        progress.stop()
                    }
                }
            }

        fun setProfile(user: User) {
            val uid = user.uid
            txtName.text = user.name
            if (args.current) {
                txtEmailTitle.isVisible = true
                txtEmail.isVisible = true
                txtEmail.text = email
                txtDateOfBirthTitle.isVisible = true
                txtDateOfBirth.isVisible = true
            }
            txtUsername.text = user.username
            txtMobile.text = user.mobile.replaceFirst("0", "+63")
            txtLocation.text = user.location
            txtSex.text = user.sex
            val dateOfBirth =
                LocalDateTime.ofInstant(Instant.ofEpochSecond(user.dateOfBirth.seconds), zone)
            txtDateOfBirth.text = dateTimeFormatter.format(dateOfBirth)

            val image = user.image
            if (image != null) {
                loadProfileImage(imageUser, storage.getUserPhoto(image))
            } else imageUser.setImageResource(R.drawable.ic_person)

            val dialog =
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.title_report_user)
                    .setMessage(R.string.txt_report_user)
                    .setPositiveButton(R.string.btn_confirm) { _, _ ->
                        firestore.addReport(uid).observe(viewLifecycleOwner) { result ->
                            when (result) {
                                is Result.Loading -> {
                                    progress.start()
                                    toolbar.menu.getItem(0).icon = progress as Drawable
                                }
                                is Result.Success -> reportUser(uid, currentUid)
                                is Result.Failure -> {
                                    showSnackbar(result.exception)
                                    toolbar.menu.getItem(0).icon = report
                                    progress.stop()
                                }
                            }
                        }
                    }
                    .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_edit -> {
                        findNavController().navigate(R.id.action_profile_to_edit_profile)
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item_report -> {
                        firestore.getReport(uid, currentUid).observe(viewLifecycleOwner) { result ->
                            when (result) {
                                is Result.Loading -> item.icon = progress as Drawable
                                is Result.Success -> {
                                    if (result.data.exists()) {
                                        showSnackbar(getString(R.string.txt_user_already_reported))
                                        item.setIcon(R.drawable.ic_report)
                                    } else dialog.show()
                                }
                                is Result.Failure -> {
                                    showSnackbar(result.exception)
                                    item.setIcon(R.drawable.ic_report)
                                }
                            }
                        }

                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
        }

        if (args.current) {
            toolbar.inflateMenu(R.menu.menu_current_profile)
            dataViewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
                setProfile(user)
            }
        } else {
            toolbar.inflateMenu(R.menu.menu_profile)
            dataViewModel.getUser().observe(viewLifecycleOwner) { user ->
                setProfile(user)
            }
        }
    }

}