package com.vt.shoppet.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.toObject
import com.vt.shoppet.R
import com.vt.shoppet.databinding.FragmentChatBinding
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.adapter.FirestoreChatAdapter
import com.vt.shoppet.util.loadProfileImage
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val binding by viewBinding(FragmentChatBinding::bind)

    private val viewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var storage: StorageRepo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerChats = binding.recyclerChats
        val txtEmpty = binding.txtEmpty

        viewModel.getFirestoreChats().observe(viewLifecycleOwner) { chats ->
            val options =
                FirestoreRecyclerOptions.Builder<Chat>()
                    .setLifecycleOwner(this)
                    .setSnapshotArray(chats)
                    .build()

            viewModel.getCurrentUser().observe(viewLifecycleOwner) { user ->
                val adapter = object : FirestoreChatAdapter(options, user) {
                    override fun onDataChanged() {
                        txtEmpty.isVisible = itemCount == 0
                    }
                }
                adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
                adapter.setActions(object : ChatActions {
                    override fun onClick(chat: Chat) = View.OnClickListener {
                        val currentIndex = if (user.uid == chat.uids[0]) 0 else 1
                        val index = if (user.uid == chat.uids[0]) 1 else 0
                        viewModel.setChat(chat)
                        recyclerChats.removeItemDecorationAt(0)
                    }

                    override fun setImage(uid: String, imageView: ImageView) {
                        firestore.getUserLiveData(uid).observe(viewLifecycleOwner) { result ->
                            when (result) {
                                is Result.Success -> {
                                    val data: User? = result.data.toObject()
                                    data?.let {
                                        val image = data.image
                                        if (image.isNotEmpty()) {
                                            loadProfileImage(imageView, storage.getUserPhoto(image))
                                        } else imageView.setImageResource(R.drawable.ic_person)
                                    }
                                }
                                is Result.Failure -> imageView.setImageResource(R.drawable.ic_person)
                            }
                        }
                    }
                })

                recyclerChats.apply {
                    addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    setAdapter(adapter)
                }
            }
        }
    }

}