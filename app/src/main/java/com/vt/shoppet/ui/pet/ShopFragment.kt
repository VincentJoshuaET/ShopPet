package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.vt.shoppet.R
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.databinding.FragmentShopBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.ui.adapter.PetAdapter
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
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val context = requireContext()
        val activity = requireActivity() as MainActivity

        val fabSell = binding.fabSell

        val recyclerPets = binding.recyclerPets
        val txtEmpty = binding.txtEmpty

        val toolbar = activity.toolbar

        if (args.posted) showSnackbar(getString(R.string.txt_upload_success))

        val adapter = PetAdapter()
        adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
        adapter.setActions(object : PetActions {
            override fun onClick(pet: Pet, view: View): View.OnClickListener =
                View.OnClickListener {
                    viewModel.setCurrentPet(pet)
                    val id = pet.id
                    view.transitionName = id
                    val extras = FragmentNavigatorExtras(view to id)
                    val action = ShopFragmentDirections.actionShopToSelected(id)
                    findNavController().navigate(action, extras)
                }

            override fun setImage(id: String, imageView: ImageView) {
                loadFirebaseImage(imageView, storage.getPetPhoto(id))
            }
        })
        recyclerPets.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
            addOnLayoutChangeListener { _, _, top, _, _, _, oldTop, _, _ ->
                if (top < oldTop) smoothScrollToPosition(oldTop)
            }
            setAdapter(adapter)
        }
        txtEmpty.isVisible = adapter.itemCount == 0

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

        findNavController().currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<Boolean>("removed").observe(viewLifecycleOwner) { removed ->
                if (removed) showSnackbar(getString(R.string.txt_removed_pet))
                remove<Boolean>("removed")
            }
            getLiveData<Boolean>("sold").observe(viewLifecycleOwner) { sold ->
                if (sold) showSnackbar(getString(R.string.txt_marked_pet_sold))
                remove<Boolean>("sold")
            }
            getLiveData<Boolean>("filter").observe(viewLifecycleOwner) { filter ->
                if (filter) {
                    viewModel.getFilteredPets().observe(viewLifecycleOwner) { filtered ->
                        adapter.submitList(filtered)
                        txtEmpty.isVisible = filtered.isEmpty()
                    }
                }
                remove<Boolean>("filter")
            }
        }

        viewModel.getFilter().observe(viewLifecycleOwner) { filter ->
            if (filter.enabled) {
                viewModel.getFilteredPets().observe(viewLifecycleOwner) { filtered ->
                    adapter.submitList(filtered)
                    txtEmpty.isVisible = filtered.isEmpty()
                }
            } else {
                viewModel.getPets().observe(viewLifecycleOwner) { pets ->
                    adapter.submitList(pets)
                    txtEmpty.isVisible = pets.isEmpty()
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

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_refresh -> {
                    recyclerPets.smoothScrollToPosition(0)
                    viewModel.resetFilter()
                    return@setOnMenuItemClickListener true
                }
                R.id.item_filter -> {
                    findNavController().navigate(R.id.action_shop_to_filter)
                    return@setOnMenuItemClickListener true
                }
                R.id.item_sort -> {
                    findNavController().navigate(R.id.action_shop_to_sort)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }
}