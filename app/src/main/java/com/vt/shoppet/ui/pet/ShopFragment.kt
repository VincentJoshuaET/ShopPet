package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.transition.TransitionManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.vt.shoppet.R
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.databinding.FragmentShopBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.adapter.FirestorePetAdapter
import com.vt.shoppet.util.*
import com.vt.shoppet.util.PermissionUtils.SELECT_PHOTO
import com.vt.shoppet.util.PermissionUtils.TAKE_PHOTO
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Runnable
import javax.inject.Inject

@AndroidEntryPoint
class ShopFragment : Fragment(R.layout.fragment_shop) {

    private val binding by viewBinding(FragmentShopBinding::bind)
    private val viewModel: DataViewModel by activityViewModels()
    private val args: ShopFragmentArgs by navArgs()

    @Inject
    lateinit var storage: StorageRepo

    private var action = 0

    private fun checkPermissions() =
        if (checkSelfPermissions()) {
            when (action) {
                SELECT_PHOTO -> selectPhoto.launch("image/*")
                TAKE_PHOTO -> findNavController().navigate(R.id.action_shop_to_camera)
                else -> null
            }
        } else requestPermissions.launch(permissions)

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.checkAllPermissions()) {
                when (action) {
                    SELECT_PHOTO -> selectPhoto.launch("image/*")
                    TAKE_PHOTO -> findNavController().navigate(R.id.action_shop_to_camera)
                }
            } else showSnackbar(getString(R.string.txt_permission_denied))
        }

    private val selectPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val action = ShopFragmentDirections.actionShopToSell(uri.toString())
            findNavController().navigate(action)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        val fabSell = binding.fabSell

        val recyclerPets = binding.recyclerPets
        val txtEmpty = binding.txtEmpty

        if (args.posted) showSnackbar(getString(R.string.txt_upload_success))

        findNavController().currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<Boolean>("removed").observe(viewLifecycleOwner) { removed ->
                if (removed) showSnackbar(getString(R.string.txt_removed_pet))
                remove<Boolean>("removed")
            }
            getLiveData<Boolean>("sold").observe(viewLifecycleOwner) { sold ->
                if (sold) showSnackbar(getString(R.string.txt_marked_pet_sold))
                remove<Boolean>("sold")
            }
        }

        val upload =
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_pet_image)
                .setItems(R.array.add_photo) { _, items ->
                    when (items) {
                        0 -> {
                            action = SELECT_PHOTO
                            checkPermissions()
                        }
                        1 -> {
                            action = TAKE_PHOTO
                            checkPermissions()
                        }
                    }
                }
                .setOnDismissListener {
                    fabSell.show()
                }
                .create()

        viewModel.getFirestorePets().observe(viewLifecycleOwner) { pets ->
            val options =
                FirestoreRecyclerOptions.Builder<Pet>()
                    .setLifecycleOwner(this)
                    .setSnapshotArray(pets)
                    .build()

            val adapter = object : FirestorePetAdapter(options) {
                override fun onDataChanged() {
                    txtEmpty.isVisible = itemCount == 0
                }
            }
            adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter.setActions(object : PetActions {
                override fun onClick(pet: Pet): View.OnClickListener =
                    View.OnClickListener {
                        viewModel.setCurrentPet(pet)
                        findNavController().navigate(R.id.action_shop_to_selected)
                    }

                override fun setImage(id: String, imageView: ImageView) {
                    loadFirebaseImage(imageView, storage.getPetPhoto(id))
                }
            })

            recyclerPets.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, 2)
                setAdapter(adapter)
                addOnLayoutChangeListener { _, _, top, _, _, _, oldTop, _, _ ->
                    if (top < oldTop) smoothScrollToPosition(oldTop)
                }
            }
        }

        fabSell.setOnClickListener {
            fabSell.hide()
            upload.show()
        }

        val runnable = Runnable {
            TransitionManager.beginDelayedTransition(binding.root, MaterialFade())
            fabSell.isVisible = true
        }

        viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
            if (user.reports < resources.getInteger(R.integer.reports)) fabSell.post(runnable)
        }

    }
}