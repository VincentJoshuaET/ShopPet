package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.vt.shoppet.R
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.databinding.FragmentOwnBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.ui.adapter.PetAdapter
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OwnFragment : Fragment(R.layout.fragment_own) {

    private val binding by viewBinding(FragmentOwnBinding::bind)
    private val dataViewModel: DataViewModel by activityViewModels()
    private val storage: StorageViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val recyclerPets = binding.recyclerPets
        val txtEmpty = binding.txtEmpty

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<Boolean>("removed")?.observe(viewLifecycleOwner) { removed ->
            if (removed) binding.snackbar(getString(R.string.txt_removed_pet)).show()
            savedStateHandle.remove<Boolean>("removed")
        }
        savedStateHandle?.getLiveData<Boolean>("sold")?.observe(viewLifecycleOwner) { sold ->
            if (sold) binding.snackbar(getString(R.string.txt_marked_pet_sold)).show()
            savedStateHandle.remove<Boolean>("sold")
        }

        val actions = object : PetActions {
            override fun onClick(pet: Pet, view: View): View.OnClickListener =
                View.OnClickListener {
                    dataViewModel.setCurrentPet(pet)
                    val id = pet.id
                    view.transitionName = id
                    val extras = FragmentNavigatorExtras(view to id)
                    val action = OwnFragmentDirections.actionOwnToSelected(id, pet.name)
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

        dataViewModel.ownPets.observe(viewLifecycleOwner) { pets ->
            adapter.submitList(pets)
            txtEmpty.isVisible = pets.isEmpty()
        }
    }

}