package com.vt.shoppet.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.google.firebase.firestore.ktx.toObject
import com.vt.shoppet.R
import com.vt.shoppet.actions.ChatActions
import com.vt.shoppet.databinding.FragmentChatBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.User
import com.vt.shoppet.ui.adapter.ChatAdapter
import com.vt.shoppet.util.loadProfileImage
import com.vt.shoppet.util.snackbar
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
            val action = object : ChatActions {
                override fun onClick(chat: Chat): View.OnClickListener = View.OnClickListener {
                    val senderIndex = if (user.uid == chat.uid[0]) 0 else 1
                    val receiverIndex = if (user.uid == chat.uid[0]) 1 else 0
                    val username =
                        if (user.uid == chat.uid[0]) chat.username[1] else chat.username[0]
                    dataViewModel.setChat(chat)
                    recyclerChats.removeItemDecorationAt(0)
                    val action =
                        ChatFragmentDirections
                            .actionChatToConversation(senderIndex, receiverIndex, username)
                    findNavController().navigate(action)
                }

                override fun setImage(uid: String, imageView: ImageView) {
                    firestore.getUserSnapshot(uid).observe(viewLifecycleOwner) { result ->
                        result.onSuccess { document ->
                            val data: User = document.toObject() ?: return@onSuccess
                            val image = data.image
                                ?: return@onSuccess imageView.setImageResource(R.drawable.ic_person)
                            loadProfileImage(imageView, storage.getUserPhoto(image))
                        }
                        result.onFailure { exception ->
                            imageView.setImageResource(R.drawable.ic_person)
                            binding.snackbar(
                                message = exception.localizedMessage,
                                owner = viewLifecycleOwner
                            ) {
                                setImage(uid, imageView)
                            }.show()
                        }
                    }
                }
            }

            val adapter = ChatAdapter(user, action)
            adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY

            recyclerChats.apply {
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                setHasFixedSize(true)
                setAdapter(adapter)
            }

            dataViewModel.chats.observe(viewLifecycleOwner) { chats ->
                adapter.submitList(chats)
                txtEmpty.isVisible = chats.isEmpty()
            }
        }

    }

}