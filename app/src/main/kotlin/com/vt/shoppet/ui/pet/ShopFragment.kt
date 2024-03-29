package com.vt.shoppet.ui.pet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.databinding.FragmentShopBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.ui.MainActivity
import com.vt.shoppet.ui.adapter.PetAdapter
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopFragment : Fragment(R.layout.fragment_shop) {

    private val binding by viewBinding(FragmentShopBinding::bind)

    private val storage: StorageViewModel by viewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    private fun takePhoto() {
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            findNavController().navigate(R.id.action_shop_to_camera)
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNavController().navigate(R.id.action_shop_to_camera)
            } else {
                binding.snackbar(
                    message = getString(R.string.txt_permission_denied),
                    anchor = binding.fabSell,
                    owner = viewLifecycleOwner
                ).show()
            }
        }

    private val selectPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val action = ShopFragmentDirections.actionShopToSell(uri.toString())
                findNavController().navigate(action)
            }
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

        val navBackStackEntry = findNavController().currentBackStackEntry
        val savedStateHandle = navBackStackEntry?.savedStateHandle
        savedStateHandle?.run {
            getLiveData<Boolean>("posted").observe(viewLifecycleOwner) { posted ->
                if (posted) {
                    binding.snackbar(getString(R.string.txt_upload_success), fabSell).show()
                }
                remove<Boolean>("posted")
            }
            getLiveData<Boolean>("removed").observe(viewLifecycleOwner) { removed ->
                if (removed) {
                    binding.snackbar(getString(R.string.txt_removed_pet), fabSell).show()
                }
                remove<Boolean>("removed")
            }
            getLiveData<Boolean>("sold").observe(viewLifecycleOwner) { sold ->
                if (sold) {
                    binding.snackbar(getString(R.string.txt_marked_pet_sold), fabSell).show()
                }
                remove<Boolean>("sold")
            }
        }

        val actions = object : PetActions {
            override fun onClick(pet: Pet, view: View): View.OnClickListener =
                View.OnClickListener {
                    dataViewModel.setCurrentPet(pet)
                    val id = pet.id
                    view.transitionName = id
                    val extras = FragmentNavigatorExtras(view to id)
                    val action = ShopFragmentDirections.actionShopToSelected(id, pet.name)
                    findNavController().navigate(action, extras)
                }

            override fun setImage(id: String, imageView: ImageView) =
                loadFirebaseImage(imageView, storage.getPetPhoto(id))
        }

        val adapter = PetAdapter(actions)
        adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY

        recyclerPets.apply {
            setHasFixedSize(true)
            setOnLayoutChangeListener()
            setAdapter(adapter)
        }

        val upload =
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_pet_image)
                .setItems(R.array.add_photo) { _, items ->
                    when (items) {
                        0 -> selectPhoto.launch("image/*")
                        1 -> takePhoto()
                    }
                }
                .setOnDismissListener {
                    fabSell.show()
                }
                .create()


        val filteredPetsObserver = Observer<List<Pet>> { filteredPets ->
            adapter.submitList(filteredPets)
            txtEmpty.isVisible = filteredPets.isEmpty()
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val filter =
                    savedStateHandle?.get<Boolean>("filter") ?: return@LifecycleEventObserver
                if (filter) {
                    dataViewModel.filteredPets.observe(viewLifecycleOwner, filteredPetsObserver)
                }
                savedStateHandle.remove<Boolean>("filter")
            }
        }

        navBackStackEntry?.lifecycle?.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry?.lifecycle?.removeObserver(observer)
            }
        })

        dataViewModel.filter.observe(viewLifecycleOwner) { filter ->
            if (filter.enabled) {
                dataViewModel.filteredPets.observe(viewLifecycleOwner, filteredPetsObserver)
            } else {
                dataViewModel.pets.observe(viewLifecycleOwner) { pets ->
                    adapter.submitList(pets)
                    txtEmpty.isVisible = pets.isEmpty()
                }
            }
        }

        fabSell.setOnClickListener {
            fabSell.hide()
            upload.show()
        }

        dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user.reports < resources.getInteger(R.integer.reports)) fabSell.show()
            else {
                binding.snackbar(getString(R.string.txt_reported), fabSell).show()
                fabSell.hide()
            }
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_refresh -> {
                    dataViewModel.resetFilter()
                    dataViewModel.filteredPets.removeObserver(filteredPetsObserver)
                    recyclerPets.smoothScrollToPosition(0)
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