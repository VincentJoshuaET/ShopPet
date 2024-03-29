package com.vt.shoppet.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.vt.shoppet.R
import com.vt.shoppet.actions.MessageActions
import com.vt.shoppet.databinding.FragmentConversationBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Message
import com.vt.shoppet.ui.adapter.MessageAdapter
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

    private val binding by viewBinding(FragmentConversationBinding::bind)
    private val args: ConversationFragmentArgs by navArgs()

    private val storage: StorageViewModel by activityViewModels()
    private val firestore: FirestoreViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private val send by lazy { getDrawable(R.drawable.ic_send) }
    private val progress by lazy { circularProgress }

    private var uri = Uri.EMPTY

    private var listener: ListenerRegistration? = null

    private fun takePhoto() {
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val file = File(requireContext().cacheDir, "${System.currentTimeMillis()}.jpg")
            uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            openCamera.launch(uri)
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) takePhoto()
            else binding.snackbar(
                message = getString(R.string.txt_permission_denied),
                owner = viewLifecycleOwner
            ).show()
        }

    private val selectPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                this.uri = uri
                loadImage()
            }
        }

    private val openCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { boolean ->
            if (boolean) loadImage()
        }

    private fun loadImage() = binding.apply {
        loadImage(imageMessage, uri)
        layoutImage.isVisible = true
        val adapter = recyclerMessages.adapter ?: return@apply
        recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
    }

    private fun clearImageView() = binding.apply {
        imageMessage.setImageDrawable(null)
        layoutImage.isVisible = false
        uri = Uri.EMPTY
        val adapter = recyclerMessages.adapter ?: return@apply
        recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
    }

    private fun sendChat(chat: Chat) {
        val txtMessage = binding.txtMessage
        val inputMessage = binding.inputMessage
        txtMessage.text = null
        txtMessage.isEnabled = false
        progress.start()
        inputMessage.endIconDrawable = progress as Drawable
        firestore.updateChat(chat).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
            result.onFailure { exception ->
                binding.snackbar(
                    message = exception.localizedMessage,
                    owner = viewLifecycleOwner
                ) {
                    sendChat(chat)
                }.show()
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
        }
    }

    private fun sendTextMessage(chat: Chat, message: Message) {
        val txtMessage = binding.txtMessage
        val inputMessage = binding.inputMessage
        txtMessage.text = null
        txtMessage.isEnabled = false
        progress.start()
        inputMessage.endIconDrawable = progress as Drawable
        firestore.sendMessage(chat, message).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                val text = if (message.message.isEmpty()) "Image" else message.message
                val read = chat.read.apply {
                    this[args.senderIndex] = true
                    this[args.receiverIndex] = false
                }
                val data =
                    chat.copy(empty = false, message = text, read = read, date = message.date)
                sendChat(data)
            }
            result.onFailure { exception ->
                binding.snackbar(
                    message = exception.localizedMessage,
                    owner = viewLifecycleOwner
                ) {
                    sendTextMessage(chat, message)
                }.show()
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
        }
    }

    private fun uploadMessagePhoto(chat: Chat, text: String, image: String) {
        val txtMessage = binding.txtMessage
        val inputMessage = binding.inputMessage
        txtMessage.text = null
        txtMessage.isEnabled = false
        progress.start()
        inputMessage.endIconDrawable = progress as Drawable
        storage.uploadMessagePhoto(image, uri).observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                clearImageView()
                val date = Timestamp.now()
                val message = Message(
                    message = text,
                    chatid = chat.id,
                    senderid = chat.uid[args.senderIndex],
                    recipientid = chat.uid[args.receiverIndex],
                    senderusername = chat.username[args.senderIndex],
                    recipientusername = chat.username[args.receiverIndex],
                    image = image,
                    date = date
                )
                sendTextMessage(chat, message)
            }
            result.onFailure { exception ->
                binding.snackbar(
                    message = exception.localizedMessage,
                    owner = viewLifecycleOwner
                ) {
                    uploadMessagePhoto(chat, text, image)
                }.show()
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = firestore.uid
        val context = requireContext()

        val recyclerMessages = binding.recyclerMessages
        val inputMessage = binding.inputMessage
        val txtMessage = binding.txtMessage

        val txtEmpty = binding.txtEmpty
        val fabRemove = binding.fabRemove

        val items = resources.getStringArray(R.array.add_photo)

        val addPhoto =
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_select_image)
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> selectPhoto.launch("image/*")
                        1 -> takePhoto()
                    }
                }
                .create()

        txtMessage.setErrorListener()

        inputMessage.setStartIconOnClickListener {
            addPhoto.show()
        }

        fabRemove.setOnClickListener {
            clearImageView()
        }

        dataViewModel.chat.observe(viewLifecycleOwner) { chat ->
            val query = firestore.getMessages(chat.id)

            val options =
                FirestoreRecyclerOptions.Builder<Message>()
                    .setLifecycleOwner(viewLifecycleOwner)
                    .setQuery(query, Message::class.java)
                    .build()

            val actions = MessageActions { id, imageView ->
                loadMessageImage(imageView, storage.getMessagePhoto(id))
            }

            val adapter = object : MessageAdapter(options, actions) {
                override fun getItemViewType(position: Int): Int =
                    if (uid == getItem(position).senderid) R.layout.item_message_from
                    else R.layout.item_message_to

                override fun onDataChanged() {
                    txtEmpty.isVisible = itemCount == 0
                    if (itemCount != 0) {
                        recyclerMessages.layoutManager?.scrollToPosition(itemCount - 1)
                    }
                }
            }

            listener = query.addSnapshotListener { value, _ ->
                if (value != null) {
                    val read = chat.read.apply { set(args.senderIndex, true) }
                    firestore.markChatAsRead(chat.id, read)
                }
            }

            recyclerMessages.adapter = adapter

            inputMessage.setEndIconOnClickListener {
                keyboard.hide()

                val text = txtMessage.text.toString()

                if (uri != Uri.EMPTY) {
                    val image = UUID.randomUUID().toString()
                    uploadMessagePhoto(chat, text, image)
                } else {
                    val date = Timestamp.now()
                    val message = Message(
                        message = text,
                        chatid = chat.id,
                        senderid = chat.uid[args.senderIndex],
                        recipientid = chat.uid[args.receiverIndex],
                        senderusername = chat.username[args.senderIndex],
                        recipientusername = chat.username[args.receiverIndex],
                        date = date
                    )
                    if (text.isNotEmpty()) sendTextMessage(chat, message)
                    else {
                        txtMessage.isEnabled = true
                        inputMessage.endIconDrawable = send
                        inputMessage.error = getString(R.string.txt_empty_message)
                        progress.stop()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        listener?.remove()
        listener = null
        super.onDestroyView()
    }

}