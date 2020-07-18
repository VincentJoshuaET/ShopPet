package com.vt.shoppet.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.google.firebase.firestore.ktx.toObject
import com.vt.shoppet.R
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.databinding.FragmentChatBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Result
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.adapter.ChatAdapter
import com.vt.shoppet.util.loadProfileImage
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val binding by viewBinding(FragmentChatBinding::bind)

    private val firestore: FirestoreViewModel by activityViewModels()
    private val storage: StorageViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerChats = binding.recyclerChats
        val txtEmpty = binding.txtEmpty

        dataViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            val adapter = ChatAdapter(user).apply {
                stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setActions(object : ChatActions {
                    override fun onClick(chat: Chat) = View.OnClickListener {
                        val senderIndex = if (user.uid == chat.uid[0]) 0 else 1
                        val receiverIndex = if (user.uid == chat.uid[0]) 1 else 0
                        dataViewModel.setChat(chat)
                        recyclerChats.removeItemDecorationAt(0)
                        val action =
                            ChatFragmentDirections
                                .actionChatToConversation(senderIndex, receiverIndex)
                        findNavController().navigate(action)
                    }

                    override fun setImage(uid: String, imageView: ImageView) =
                        firestore.getUserSnapshot(uid).observe(viewLifecycleOwner) { result ->
                            when (result) {
                                is Result.Success -> {
                                    val data: User? = result.data.toObject()
                                    data?.apply {
                                        val image = image
                                        if (image != null) {
                                            loadProfileImage(imageView, storage.getUserPhoto(image))
                                        } else {
                                            imageView.setImageResource(R.drawable.ic_person)
                                        }
                                    }
                                }
                                is Result.Failure -> imageView.setImageResource(R.drawable.ic_person)
                            }
                        }
                })
            }

            recyclerChats.apply {
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                setAdapter(adapter)
            }

            dataViewModel.chats.observe(viewLifecycleOwner) { chats ->
                adapter.submitList(chats)
                txtEmpty.isVisible = chats.isEmpty()
            }
        }

    }

}