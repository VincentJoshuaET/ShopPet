package com.vt.shoppet.ui.chat

import android.content.ContentValues
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.vt.shoppet.R
import com.vt.shoppet.actions.MessageActions
import com.vt.shoppet.databinding.FragmentConversationBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Message
import com.vt.shoppet.ui.adapter.MessageAdapter
import com.vt.shoppet.util.*
import com.vt.shoppet.util.PermissionUtils.SELECT_PHOTO
import com.vt.shoppet.util.PermissionUtils.TAKE_PHOTO
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

    private val binding by viewBinding(FragmentConversationBinding::bind)
    private val args: ConversationFragmentArgs by navArgs()

    private val auth: AuthViewModel by activityViewModels()
    private val storage: StorageViewModel by activityViewModels()
    private val firestore: FirestoreViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    @Inject
    lateinit var keyboard: KeyboardUtils

    private lateinit var layoutImage: CoordinatorLayout
    private lateinit var imageMessage: ShapeableImageView
    private lateinit var inputMessage: TextInputLayout
    private lateinit var txtMessage: TextInputEditText
    private lateinit var recyclerMessages: RecyclerView

    private lateinit var send: Drawable
    private lateinit var progress: Animatable

    private var uri = Uri.EMPTY
    private var action = 0

    private fun checkPermissions() =
        if (checkSelfPermissions()) {
            when (action) {
                SELECT_PHOTO -> selectPhoto.launch("image/*")
                TAKE_PHOTO -> {
                    val contentResolver = requireContext().contentResolver
                    uri = contentResolver.insert(EXTERNAL_CONTENT_URI, ContentValues())
                    openCamera.launch(uri)
                }
                else -> null
            }
        } else requestPermissions.launch(permissions)

    private val requestPermissions: ActivityResultLauncher<Array<String>>
        get() = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.checkAllPermissions()) {
                when (action) {
                    SELECT_PHOTO -> selectPhoto.launch("image/*")
                    TAKE_PHOTO -> {
                        val contentResolver = requireContext().contentResolver
                        uri = contentResolver.insert(EXTERNAL_CONTENT_URI, ContentValues())
                        openCamera.launch(uri)
                    }
                }
            } else {
                showActionSnackbar(getString(R.string.txt_permission_denied)) {
                    requestPermissions.launch(permissions)
                }
            }
        }

    private val selectPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                uri = it
                loadImage()
            }
        }

    private val openCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { boolean ->
            if (boolean) loadImage()
        }

    private fun loadImage() {
        loadImage(imageMessage, uri)
        layoutImage.isVisible = true
        val adapter = recyclerMessages.adapter ?: return
        recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
    }

    private fun clearImageView() {
        imageMessage.setImageDrawable(null)
        layoutImage.isVisible = false
        uri = Uri.EMPTY
        val adapter = recyclerMessages.adapter ?: return
        recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
    }

    private fun sendChat(chat: Chat) {
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
                showActionSnackbar(exception) {
                    sendChat(chat)
                }
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
        }
    }

    private fun sendTextMessage(chat: Chat, message: Message) {
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
                showActionSnackbar(exception) {
                    sendTextMessage(chat, message)
                }
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
        }
    }

    private fun uploadMessagePhoto(chat: Chat, text: String, image: String) {
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
                showActionSnackbar(exception) {
                    uploadMessagePhoto(chat, text, image)
                }
                txtMessage.isEnabled = true
                inputMessage.endIconDrawable = send
                progress.stop()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = auth.uid()
        val context = requireContext()

        recyclerMessages = binding.recyclerMessages
        layoutImage = binding.layoutImage
        imageMessage = binding.imageMessage
        inputMessage = binding.inputMessage
        txtMessage = binding.txtMessage
        progress = circularProgress()
        send = getDrawable(R.drawable.ic_send)

        val txtEmpty = binding.txtEmpty
        val fabRemove = binding.fabRemove

        val items = resources.getStringArray(R.array.add_photo)

        val addPhoto =
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.title_select_image)
                .setItems(items) { _, which ->
                    when (which) {
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
                    .setLifecycleOwner(this)
                    .setQuery(query, Message::class.java)
                    .build()

            val actions = MessageActions { id, imageView ->
                loadFirebaseImage(imageView, storage.getMessagePhoto(id))
            }

            val adapter = object : MessageAdapter(options, actions) {
                override fun getItemViewType(position: Int): Int =
                    if (uid == getItem(position).senderid) R.layout.item_message_from
                    else R.layout.item_message_to

                override fun onDataChanged() {
                    txtEmpty.isVisible = itemCount == 0
                    if (itemCount != 0) {
                        recyclerMessages.layoutManager?.scrollToPosition(itemCount - 1)
                        val data = chat.copy(
                            read = chat.read.apply {
                                this[args.senderIndex] = true
                            }
                        )
                        firestore.updateChat(data)
                    }
                }
            }

            recyclerMessages.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context).apply {
                    stackFromEnd = true
                }
                setAdapter(adapter)
            }

            inputMessage.setEndIconOnClickListener {
                keyboard.hide(this)

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

}