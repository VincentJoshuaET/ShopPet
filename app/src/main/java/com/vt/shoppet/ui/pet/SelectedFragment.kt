package com.vt.shoppet.ui.pet

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentSelectedBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectedFragment : Fragment(R.layout.fragment_selected) {

    private val binding by viewBinding(FragmentSelectedBinding::bind)
    private val viewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var storage: StorageRepo

    private fun MaterialButton.starPet() =
        apply {
            isClickable = true
            setIconResource(R.drawable.ic_starred)
            text = getString(R.string.lbl_starred)
        }

    private fun MaterialButton.unstarPet() =
        apply {
            isClickable = true
            setIconResource(R.drawable.ic_unstarred)
            text = getString(R.string.lbl_unstarred)
        }

    private fun removePetPhoto(id: String) =
        storage.removePetPhoto(id).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> findNavController().popBackStack()
                is Result.Failure -> showSnackbar(result.exception)
            }
        }

    private fun soldDialog(id: String) =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_mark_as_sold)
            .setMessage(R.string.txt_mark_pet_sold)
            .setPositiveButton(R.string.btn_no) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_yes) { _, _ ->
                firestore.markPetAsSold(id).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Result.Success -> findNavController().popBackStack()
                        is Result.Failure -> showSnackbar(result.exception)
                    }
                }
            }
            .create()

    private fun removeDialog(id: String, image: String) =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_remove)
            .setMessage(R.string.txt_remove_pet)
            .setPositiveButton(R.string.btn_no) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_yes) { _, _ ->
                firestore.removePet(id).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Result.Success -> removePetPhoto(image)
                        is Result.Failure -> showSnackbar(result.exception)
                    }
                }
            }
            .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var starred = false

        val imagePet = binding.imagePet
        val fabChatSold = binding.fabChatSold

        val btnStar = binding.btnStar as MaterialButton
        val btnGroup = binding.btnGrp
        val btnEdit = binding.btnEdit
        val btnRemove = binding.btnRemove
        val txtName = binding.txtName
        val txtPrice = binding.txtPrice
        val txtType = binding.txtType
        val txtSex = binding.txtSex
        val txtAge = binding.txtAge
        val txtBreed = binding.txtBreed
        val layoutCatsDogs = binding.layoutCatsDogs
        val txtVaccineStatus = binding.txtVaccineStatus
        val txtMedicalRecords = binding.txtMedicalRecords
        val txtDescription = binding.txtDescription
        val txtDate = binding.txtDate
        val cardSeller = binding.cardSeller
        val imageSeller = binding.imageSeller
        val txtSeller = binding.txtSeller

        val context = requireContext()
        val progress = circularProgress(context)

        val types = resources.getStringArray(R.array.type)

        var currentIndex = 0
        var index = 1

        viewModel.getCurrentPet().observe(viewLifecycleOwner) { pet ->
            layoutCatsDogs.isVisible = pet.type == types[1] || pet.type == types[2]
            fabChatSold.isGone = pet.sold

            loadFirebaseImage(imagePet, storage.getPetPhoto(pet.image))

            txtName.text = pet.name
            val price = "${getString(R.string.sym_currency)} ${pet.price}"
            txtPrice.text = price
            txtType.text = pet.type
            txtSex.text = pet.sex
            txtAge.text = pet.dateOfBirth.calculateAge()
            txtBreed.text = pet.breed
            txtVaccineStatus.text = pet.vaccineStatus
            txtMedicalRecords.text = pet.medicalRecords
            txtDescription.text = pet.description
            txtDate.text = pet.date.calculatePostDuration(pet.sold)
            txtSeller.text = pet.username

            viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
                if (user.uid == pet.uid) {
                    btnGroup.isVisible = true
                    fabChatSold.setImageResource(R.drawable.ic_check)
                } else {
                    btnStar.isVisible = true
                    fabChatSold.setImageResource(R.drawable.ic_chat)
                    firestore.checkStarredPet(pet.id).observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Result.Loading -> {
                                progress.start()
                                btnStar.isClickable = false
                                btnStar.icon = progress as Drawable
                                btnStar.text = getString(R.string.lbl_loading)
                            }
                            is Result.Success -> {
                                starred = result.data.exists()
                                if (starred) {
                                    btnStar.starPet()
                                    progress.stop()
                                } else {
                                    btnStar.unstarPet()
                                    progress.stop()
                                }
                            }
                            is Result.Failure -> {
                                btnStar.isClickable = false
                                progress.stop()
                                btnStar.unstarPet()
                            }
                        }
                    }
                    firestore.checkChat(user.uid, pet.uid).observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data.isEmpty) {
                                    val chat = Chat(
                                        "${user.uid}${pet.uid}",
                                        null,
                                        listOf(user.uid, pet.uid),
                                        listOf(user.username, pet.username),
                                        listOf(true, true),
                                        true
                                    )
                                    firestore.createChat(chat)
                                    viewModel.setChat(chat)
                                } else {
                                    val chats: List<Chat> = result.data.toObjects()
                                    viewModel.setChat(chats.first())
                                    currentIndex = chats.first().uids.indexOf(pet.uid)
                                    index = chats.first().uids.indexOf(user.uid)
                                }
                            }
                        }
                    }
                }

                firestore.getUserLiveData(pet.uid).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Result.Success -> {
                            val data: User? = result.data.toObject()
                            if (data != null) {
                                viewModel.setUser(data)
                                val image = data.image
                                if (image.isNotEmpty()) {
                                    loadProfileImage(imageSeller, storage.getUserPhoto(image))
                                } else imageSeller.setImageResource(R.drawable.ic_person)
                            } else imageSeller.setImageResource(R.drawable.ic_person)

                            fabChatSold.setOnClickListener {
                                if (pet.uid == user.uid) soldDialog(pet.id).show()
                            }

                            cardSeller.setOnClickListener {
                                val action =
                                    SelectedFragmentDirections.actionSelectedToProfile(pet.uid == user.uid)
                                findNavController().navigate(action)
                            }
                        }
                        is Result.Failure -> imageSeller.setImageResource(R.drawable.ic_person)
                    }
                }
            }

            btnStar.setOnClickListener {
                if (starred) {
                    firestore.starPet(pet).observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Result.Loading -> {
                                progress.start()
                                btnStar.isClickable = false
                                btnStar.icon = progress as Drawable
                                btnStar.text = getString(R.string.lbl_loading)
                            }
                            is Result.Success -> {
                                showSnackbar(getString(R.string.txt_unstarred))
                                starred = false
                                btnStar.unstarPet()
                                progress.stop()
                            }
                            is Result.Failure -> {
                                showSnackbar(result.exception)
                                starred = true
                                btnStar.starPet()
                                progress.stop()
                            }
                        }
                    }
                } else {
                    firestore.unstarPet(pet.id).observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Result.Loading -> {
                                progress.start()
                                btnStar.isClickable = false
                                btnStar.icon = progress as Drawable
                                btnStar.text = getString(R.string.lbl_loading)
                            }
                            is Result.Success -> {
                                showSnackbar(getString(R.string.txt_starred))
                                starred = true
                                btnStar.starPet()
                                progress.stop()
                            }
                            is Result.Failure -> {
                                showSnackbar(result.exception)
                                starred = false
                                btnStar.unstarPet()
                                progress.stop()
                            }
                        }
                    }
                }
            }

            btnRemove.setOnClickListener {
                btnGroup.clearChecked()
                removeDialog(pet.id, pet.image).show()
            }

            btnEdit.setOnClickListener {
                btnGroup.clearChecked()
            }
        }
    }

    override fun onDestroy() {
        viewModel.clearUser()
        super.onDestroy()
    }

}