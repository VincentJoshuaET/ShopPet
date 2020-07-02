package com.vt.shoppet.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentOwnBinding
import com.vt.shoppet.actions.PetActions
import com.vt.shoppet.model.Pet
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.adapter.FirestorePetAdapter
import com.vt.shoppet.util.loadFirebaseImage
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

        val recyclerPets = binding.recyclerPets
        val txtEmpty = binding.txtEmpty

        viewModel.getFirestoreOwnPets().observe(viewLifecycleOwner) { pets ->
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
                        findNavController().navigate(R.id.action_own_to_selected)
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
    }

}