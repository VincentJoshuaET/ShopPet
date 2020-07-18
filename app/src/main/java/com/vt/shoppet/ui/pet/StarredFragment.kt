package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.vt.shoppet.R
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.databinding.FragmentStarredBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.ui.adapter.PetAdapter
import com.vt.shoppet.util.loadFirebaseImage
import com.vt.shoppet.util.setOnLayoutChangeListener
import com.vt.shoppet.util.showSnackbar
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StarredFragment : Fragment(R.layout.fragment_starred) {

    private val binding by viewBinding(FragmentStarredBinding::bind)
    private val viewModel: DataViewModel by activityViewModels()

    private val storage: StorageViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerPets = binding.recyclerPets
        val txtEmpty = binding.txtEmpty

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("removed")?.observe(viewLifecycleOwner) { removed ->
            if (removed) showSnackbar(getString(R.string.txt_removed_pet))
            savedStateHandle.remove<Boolean>("removed")
        }
        savedStateHandle?.getLiveData<Boolean>("sold")?.observe(viewLifecycleOwner) { sold ->
            if (sold) showSnackbar(getString(R.string.txt_marked_pet_sold))
            savedStateHandle.remove<Boolean>("sold")
        }

        val adapter = PetAdapter().apply {
            stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
            setActions(object : PetActions {
                override fun onClick(pet: Pet, view: View): View.OnClickListener =
                    View.OnClickListener {
                        viewModel.setCurrentPet(pet)
                        val id = pet.id
                        view.transitionName = id
                        val extras = FragmentNavigatorExtras(view to id)
                        val action = StarredFragmentDirections.actionStarredToSelected(id)
                        findNavController().navigate(action, extras)
                    }

                override fun setImage(id: String, imageView: ImageView) {
                    loadFirebaseImage(imageView, storage.getPetPhoto(id))
                }
            })
        }

        recyclerPets.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
            setOnLayoutChangeListener()
            setAdapter(adapter)
        }

        viewModel.starredPets.observe(viewLifecycleOwner) { pets ->
            adapter.submitList(pets)
            txtEmpty.isVisible = pets.isEmpty()
        }
    }

}