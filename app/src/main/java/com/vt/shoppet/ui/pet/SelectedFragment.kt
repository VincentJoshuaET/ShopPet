package com.vt.shoppet.ui.pet

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentSelectedBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Pet
import com.vt.shoppet.model.User
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SelectedFragment : Fragment(R.layout.fragment_selected) {

    private val binding by viewBinding(FragmentSelectedBinding::bind)

    private val firestore: FirestoreViewModel by activityViewModels()
    private val storage: StorageViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    private lateinit var imagePet: ShapeableImageView
    private lateinit var fabChatSold: FloatingActionButton
    private lateinit var btnStar: MaterialButton
    private lateinit var cardSeller: MaterialCardView
    private lateinit var imageSeller: ShapeableImageView
    private lateinit var progress: Animatable
    private lateinit var chat: Drawable

    private var starred = false

    private fun starPetButton() =
        btnStar.apply {
            isClickable = true
            setIconResource(R.drawable.ic_starred)
            text = getString(R.string.lbl_starred)
        }

    private fun unstarPetButton() =
        btnStar.apply {
            isClickable = true
            setIconResource(R.drawable.ic_unstarred)
            text = getString(R.string.lbl_unstarred)
        }

    private fun removePetPhoto(id: String) {
        storage.removePetPhoto(id).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("removed", true)
                findNavController().popBackStack()
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    removePetPhoto(id)
                }
            }
        }
    }

    private fun soldDialog(id: String): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_mark_as_sold)
            .setMessage(R.string.txt_mark_pet_sold)
            .setPositiveButton(R.string.btn_no, null)
            .setNegativeButton(R.string.btn_yes) { _, _ ->
                progress.start()
                fabChatSold.setImageDrawable(progress as Drawable)
                fabChatSold.isClickable = false
                firestore.markSoldPet(id).observe(viewLifecycleOwner) { result ->
                    result.onSuccess {
                        findNavController().run {
                            previousBackStackEntry?.savedStateHandle?.set("sold", true)
                            popBackStack()
                        }
                    }
                    result.onFailure { exception ->
                        showActionSnackbar(exception) {
                            soldDialog(id).show()
                        }
                        fabChatSold.setImageDrawable(chat)
                        fabChatSold.isClickable = true
                        progress.stop()
                    }
                }
            }
            .create()
    }

    private fun removeDialog(id: String, image: String): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_remove)
            .setMessage(R.string.txt_remove_pet)
            .setPositiveButton(R.string.btn_no, null)
            .setNegativeButton(R.string.btn_yes) { _, _ ->
                firestore.removePet(id).observe(viewLifecycleOwner) { result ->
                    result.onSuccess {
                        removePetPhoto(image)
                    }
                    result.onFailure { exception ->
                        showActionSnackbar(exception) {
                            removeDialog(id, image).show()
                        }
                    }
                }
            }
            .create()
    }

    private fun checkStarredPet(id: String) {
        progress.start()
        btnStar.isClickable = false
        btnStar.icon = progress as Drawable
        btnStar.text = getString(R.string.lbl_loading)
        firestore.checkStarredPet(id).observe(viewLifecycleOwner) { result ->
            result.onSuccess { document ->
                starred = document.exists()
                if (starred) {
                    starPetButton()
                    progress.stop()
                } else {
                    unstarPetButton()
                    progress.stop()
                }
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    checkStarredPet(id)
                }
                btnStar.isClickable = false
                progress.stop()
                unstarPetButton()
            }
        }
    }

    private fun createChat(chat: Chat) {
        firestore.createChat(chat).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                dataViewModel.setChat(chat)
                val action =
                    SelectedFragmentDirections.actionSelectedToConversation(0, 1, chat.username[0])
                findNavController().navigate(action)
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    createChat(chat)
                }
            }
        }
    }

    private fun checkChat(pet: Pet, user: User) {
        progress.start()
        fabChatSold.setImageDrawable(progress as Drawable)
        fabChatSold.isClickable = false
        firestore.checkChat(user.uid, pet.uid).observe(viewLifecycleOwner) { result ->
            result.onSuccess { snapshots ->
                if (snapshots.isEmpty) {
                    val chat = Chat(
                        id = "${user.uid}${pet.uid}",
                        uid = listOf(user.uid, pet.uid),
                        username = listOf(user.username, pet.username),
                        read = mutableListOf(true, true),
                        empty = true
                    )
                    createChat(chat)
                } else {
                    val chats: List<Chat> = snapshots.toObjects()
                    dataViewModel.setChat(chats.first())
                    val senderIndex = chats.first().uid.indexOf(user.uid)
                    val receiverIndex = chats.first().uid.indexOf(pet.uid)
                    val username = chats.first().username[receiverIndex]
                    val action =
                        SelectedFragmentDirections
                            .actionSelectedToConversation(senderIndex, receiverIndex, username)
                    findNavController().navigate(action)
                }
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    checkChat(pet, user)
                }
                fabChatSold.setImageDrawable(chat)
                fabChatSold.isClickable = true
                progress.stop()
            }
        }
    }

    private fun getUser(pet: Pet, user: User) {
        firestore.getUserSnapshot(pet.uid).observe(viewLifecycleOwner) { result ->
            result.onSuccess { document ->
                val data: User = document.toObject()
                    ?: return@observe imageSeller.setImageResource(R.drawable.ic_person)
                dataViewModel.setUser(data)
                val image =
                    data.image ?: return@observe imageSeller.setImageResource(R.drawable.ic_person)
                loadProfileImage(imageSeller, storage.getUserPhoto(image))
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    getUser(pet, user)
                }
                imageSeller.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun starPet(pet: Pet) {
        progress.start()
        btnStar.isClickable = false
        btnStar.icon = progress as Drawable
        btnStar.text = getString(R.string.lbl_loading)
        firestore.starPet(pet).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar(getString(R.string.txt_starred))
                starred = true
                starPetButton()
                progress.stop()
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    starPet(pet)
                }
                starred = false
                unstarPetButton()
                progress.stop()
            }
        }
    }

    private fun unstarPet(id: String) {
        progress.start()
        btnStar.isClickable = false
        btnStar.icon = progress as Drawable
        btnStar.text = getString(R.string.lbl_loading)
        firestore.unstarPet(id).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showSnackbar(getString(R.string.txt_unstarred))
                starred = false
                unstarPetButton()
                progress.stop()
            }
            result.onFailure { exception ->
                showActionSnackbar(exception) {
                    unstarPet(id)
                }
                starred = true
                starPetButton()
                progress.stop()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentContainerView
            fadeMode = MaterialContainerTransform.FADE_MODE_THROUGH
            interpolator = FastOutSlowInInterpolator()
            duration = 500
            isElevationShadowEnabled = false
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentContainerView
            fadeMode = MaterialContainerTransform.FADE_MODE_THROUGH
            interpolator = FastOutSlowInInterpolator()
            duration = 500
            isElevationShadowEnabled = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = circularProgress()
        btnStar = binding.btnStar
        imagePet = binding.imagePet
        fabChatSold = binding.fabChatSold
        cardSeller = binding.cardSeller
        imageSeller = binding.imageSeller

        chat = getDrawable(R.drawable.ic_chat)

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
        val txtSeller = binding.txtSeller

        val types = resources.getStringArray(R.array.type)

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("edited")?.observe(viewLifecycleOwner) { edited ->
            if (edited) showSnackbar(getString(R.string.txt_pet_updated))
            savedStateHandle.remove<Boolean>("edited")
        }

        dataViewModel.currentPet.observe(viewLifecycleOwner) { pet ->
            binding.root.transitionName = pet.id
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

            dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
                if (user.uid == pet.uid) {
                    btnGroup.isVisible = true
                    fabChatSold.setImageResource(R.drawable.ic_check)
                } else {
                    btnStar.isVisible = true
                    fabChatSold.setImageResource(R.drawable.ic_chat)
                    checkStarredPet(pet.id)
                }

                fabChatSold.setOnClickListener {
                    if (pet.uid == user.uid) soldDialog(pet.id).show()
                    else checkChat(pet, user)
                }

                cardSeller.setOnClickListener {
                    val action =
                        SelectedFragmentDirections.actionSelectedToProfile(pet.uid == user.uid)
                    findNavController().navigate(action)
                }

                getUser(pet, user)
            }

            btnStar.setOnClickListener {
                if (starred) unstarPet(pet.id)
                else starPet(pet)
            }

            btnRemove.setOnClickListener {
                btnGroup.clearChecked()
                removeDialog(pet.id, pet.image).show()
            }

            btnEdit.setOnClickListener {
                btnGroup.clearChecked()
                findNavController().navigate(R.id.action_selected_to_edit_pet)
            }
        }
    }

}