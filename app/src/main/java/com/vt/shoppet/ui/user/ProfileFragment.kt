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
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val args: ProfileFragmentArgs by navArgs()
    private val viewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var storage: StorageRepo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val toolbar = activity.toolbar

        val context = requireContext()
        val progress = circularProgress(context)

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

        val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

        fun reportUser(uid: String, currentUid: String) =
            firestore.reportUser(uid, currentUid).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Result.Loading -> {
                        progress.start()
                        toolbar.menu.findItem(R.id.item_report).icon = progress as Drawable
                    }
                    is Result.Success -> {
                        toolbar.menu.findItem(R.id.item_report).setIcon(R.drawable.ic_report)
                        progress.stop()
                        showSnackbar(getString(R.string.txt_reported_user))
                    }
                    is Result.Failure -> {
                        showSnackbar(result.exception)
                        toolbar.menu.findItem(R.id.item_report).setIcon(R.drawable.ic_report)
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
            val dateOfBirth = LocalDateTime.ofInstant(user.dateOfBirth.toDate().toInstant(), zone)
            txtDateOfBirth.text = dateTimeFormatter.format(dateOfBirth)

            val image = user.image
            if (image.isNotEmpty()) {
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
                                    toolbar.menu.findItem(R.id.item_report).icon = progress as Drawable
                                }
                                is Result.Success -> reportUser(uid, currentUid)
                                is Result.Failure -> {
                                    showSnackbar(result.exception)
                                    toolbar.menu.findItem(R.id.item_report).setIcon(R.drawable.ic_report)
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
                                    if (result.data.exists()) dialog.show()
                                    else {
                                        showSnackbar(getString(R.string.txt_user_already_reported))
                                        item.setIcon(R.drawable.ic_report)
                                    }
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
            viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
                setProfile(user)
            }
        } else {
            toolbar.inflateMenu(R.menu.menu_profile)
            viewModel.getUser().observe(viewLifecycleOwner) { user ->
                setProfile(user)
            }
        }
    }

}