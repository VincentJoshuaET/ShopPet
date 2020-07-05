package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
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
import com.vt.shoppet.databinding.FragmentOwnBinding
import com.vt.shoppet.model.Pet
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.adapter.PetAdapter
import com.vt.shoppet.util.loadFirebaseImage
import com.vt.shoppet.util.showSnackbar
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OwnFragment : Fragment(R.layout.fragment_own) {

    private val binding by viewBinding(FragmentOwnBinding::bind)
    private val viewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var storage: StorageRepo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val recyclerPets = binding.recyclerPets
        val txtEmpty = binding.txtEmpty

        val handle = findNavController().currentBackStackEntry?.savedStateHandle

        handle?.getLiveData<Boolean>("removed")?.observe(viewLifecycleOwner) { removed ->
            if (removed) showSnackbar(getString(R.string.txt_removed_pet))
            handle.remove<Boolean>("removed")
        }
        handle?.getLiveData<Boolean>("sold")?.observe(viewLifecycleOwner) { sold ->
            if (sold) showSnackbar(getString(R.string.txt_marked_pet_sold))
            handle.remove<Boolean>("sold")
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
                        val action = OwnFragmentDirections.actionOwnToSelected(id)
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
            setAdapter(adapter)
            addOnLayoutChangeListener { _, _, top, _, _, _, oldTop, _, _ ->
                if (top < oldTop) smoothScrollToPosition(oldTop)
            }
        }

        viewModel.getOwnPets().observe(viewLifecycleOwner) { pets ->
            adapter.submitList(pets)
            txtEmpty.isVisible = pets.isEmpty()
        }
    }

}