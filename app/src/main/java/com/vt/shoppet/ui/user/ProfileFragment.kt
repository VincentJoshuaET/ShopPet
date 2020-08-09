package com.vt.shoppet.ui.user

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentProfileBinding
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

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val args: ProfileFragmentArgs by navArgs()

    private val auth: AuthViewModel by viewModels()
    private val firestore: FirestoreViewModel by viewModels()
    private val storage: StorageViewModel by viewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    private lateinit var toolbar: MaterialToolbar
    private val progress by lazy { circularProgress() }
    private val report by lazy { getDrawable(R.drawable.ic_report) }

    private fun reportUser(uid: String, currentUid: String) {
        progress.start()
        toolbar.menu.getItem(0).icon = progress as Drawable
        firestore.reportUser(uid, currentUid).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                toolbar.menu.clear()
                progress.stop()
                binding.snackbar(getString(R.string.txt_reported_user)).show()
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    reportUser(uid, currentUid)
                }.show()
                toolbar.menu.getItem(0).icon = report
                progress.stop()
            }
        }
    }

    private fun addReport(uid: String, currentUid: String) {
        progress.start()
        toolbar.menu.getItem(0).icon = progress as Drawable
        firestore.addReport(uid).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                reportUser(uid, currentUid)
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    addReport(uid, currentUid)
                }.show()
                toolbar.menu.getItem(0).icon = report
                progress.stop()
            }
        }
    }

    private fun dialog(uid: String, currentUid: String) =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_report_user)
            .setMessage(R.string.txt_report_user)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                addReport(uid, currentUid)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

    private fun getReport(uid: String, currentUid: String) {
        firestore.getReport(uid, currentUid).observe(viewLifecycleOwner) { result ->
            result.onSuccess { document ->
                if (!document.exists()) toolbar.inflateMenu(R.menu.menu_profile)
            }
            result.onFailure { exception ->
                binding.snackbar(message = exception.localizedMessage, owner = viewLifecycleOwner) {
                    getReport(uid, currentUid)
                }.show()
                toolbar.menu.getItem(0).setIcon(R.drawable.ic_report)
            }
        }
    }

    private fun setProfile(user: User, currentUid: String) = binding.apply {
        val uid = user.uid
        txtName.text = user.name
        if (args.current) {
            txtEmailTitle.isVisible = true
            txtEmail.isVisible = true
            txtEmail.text = auth.email()
            txtDateOfBirthTitle.isVisible = true
            txtDateOfBirth.isVisible = true
        }
        txtUsername.text = user.username
        txtMobile.text = user.mobile
        txtLocation.text = user.location
        txtSex.text = user.sex
        val dateOfBirth =
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(user.dateOfBirth.seconds),
                localZoneId
            )
        txtDateOfBirth.text = dateOfBirthFormatter.format(dateOfBirth)

        val image = user.image
        if (image != null) {
            loadProfileImage(imageUser, storage.getUserPhoto(image))
        } else imageUser.setImageResource(R.drawable.ic_person)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_edit -> {
                    findNavController().navigate(R.id.action_profile_to_edit_profile)
                    return@setOnMenuItemClickListener true
                }
                R.id.item_report -> {
                    dialog(uid, currentUid).show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        toolbar = activity.toolbar

        val currentUid = firestore.uid

        if (args.current) {
            toolbar.inflateMenu(R.menu.menu_current_profile)
            dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
                setProfile(user, currentUid)
            }
        } else {
            dataViewModel.user.observe(viewLifecycleOwner) { user ->
                setProfile(user, currentUid)
                getReport(user.uid, currentUid)
            }
        }
    }

}