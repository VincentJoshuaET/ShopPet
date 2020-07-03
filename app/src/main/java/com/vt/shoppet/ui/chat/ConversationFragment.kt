package com.vt.shoppet.ui.chat

import android.content.ContentValues
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.Timestamp
import com.vt.shoppet.R
import com.vt.shoppet.actions.MessageActions
import com.vt.shoppet.databinding.FragmentConversationBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.model.Message
import com.vt.shoppet.model.Result
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.ui.adapter.FirestoreMessageAdapter
import com.vt.shoppet.util.*
import com.vt.shoppet.util.PermissionUtils.SELECT_PHOTO
import com.vt.shoppet.util.PermissionUtils.TAKE_PHOTO
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

    private val binding by viewBinding(FragmentConversationBinding::bind)

    private val viewModel: DataViewModel by activityViewModels()

    private val args: ConversationFragmentArgs by navArgs()

    @Inject
    lateinit var keyboard: KeyboardUtils

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var firestore: FirestoreRepo

    @Inject
    lateinit var storage: StorageRepo

    private lateinit var adapter: FirestoreMessageAdapter

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
                SELECT_PHOTO -> choosePhoto.launch("image/*")
                TAKE_PHOTO -> {
                    uri = requireContext().contentResolver.insert(
                        EXTERNAL_CONTENT_URI,
                        ContentValues()
                    )
                    openCamera.launch(uri)
                }
                else -> null
            }
        } else requestPermissions.launch(permissions)

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.checkAllPermissions()) {
                when (action) {
                    SELECT_PHOTO -> choosePhoto.launch("image/*")
                    TAKE_PHOTO -> {
                        uri = requireContext().contentResolver.insert(
                            EXTERNAL_CONTENT_URI,
                            ContentValues()
                        )
                        openCamera.launch(uri)
                    }
                }
            } else showSnackbar(getString(R.string.txt_permission_denied))
        }

    private val choosePhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri = it
            loadImage()
        }

    private val openCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { boolean ->
            if (boolean) loadImage()
        }

    private fun loadImage() {
        loadImage(imageMessage, uri)
        layoutImage.isVisible = true
        recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
    }

    private fun clearImageView() {
        imageMessage.setImageDrawable(null)
        layoutImage.isVisible = false
        recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
        uri = Uri.EMPTY
    }

    private fun updateChat(chat: Chat) {
        val read = mutableListOf(false, false)
        read[args.senderIndex] = true
        read[args.receiverIndex] = chat.read[args.receiverIndex]
        firestore.updateChat(chat, read)
    }

    private fun sendChat(chat: Chat) =
        firestore.sendChat(chat).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    txtMessage.text = null
                    txtMessage.isEnabled = false
                    progress.start()
                    inputMessage.endIconDrawable = progress as Drawable
                }
                is Result.Success -> {
                    txtMessage.isEnabled = true
                    inputMessage.endIconDrawable = send
                    progress.stop()
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    txtMessage.isEnabled = true
                    inputMessage.endIconDrawable = send
                    progress.stop()
                }
            }
        }

    private fun sendTextMessage(chat: Chat, message: Message) =
        firestore.sendMessage(chat, message).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    txtMessage.text = null
                    txtMessage.isEnabled = false
                    progress.start()
                    inputMessage.endIconDrawable = progress as Drawable
                }
                is Result.Success -> {
                    val text = if (message.message.isEmpty()) "Image" else message.message
                    val read = mutableListOf(true, false)
                    read[args.senderIndex] = true
                    read[args.receiverIndex] = false
                    val data =
                        chat.copy(empty = false, message = text, read = read, date = message.date)
                    sendChat(data)
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    txtMessage.isEnabled = true
                    inputMessage.endIconDrawable = send
                    progress.stop()
                }
            }
        }

    private fun uploadMessagePhoto(chat: Chat, text: String, id: String) =
        storage.uploadMessagePhoto(id, uri).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    txtMessage.text = null
                    txtMessage.isEnabled = false
                    progress.start()
                    inputMessage.endIconDrawable = progress as Drawable
                }
                is Result.Success -> {
                    clearImageView()
                    val date = Timestamp.now()
                    val message = Message(
                        text,
                        chat.id,
                        chat.uid[args.senderIndex],
                        chat.uid[args.receiverIndex],
                        chat.username[args.senderIndex],
                        chat.username[args.receiverIndex],
                        id,
                        date
                    )
                    sendTextMessage(chat, message)
                }
                is Result.Failure -> {
                    showSnackbar(result.exception)
                    txtMessage.isEnabled = true
                    inputMessage.endIconDrawable = send
                    progress.stop()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
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
        progress = circularProgress(context)
        send = resources.getDrawable(R.drawable.ic_send, context.theme)

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

        viewModel.getChat().observe(viewLifecycleOwner) { chat ->
            val runnable = Runnable {
                recyclerMessages.layoutManager?.scrollToPosition(adapter.itemCount - 1)
                updateChat(chat)
            }

            val listener =
                View.OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                    if (bottom < oldBottom) recyclerMessages.postDelayed(runnable, 100)
                }

            val query = firestore.getConversation(chat.id)

            val options =
                FirestoreRecyclerOptions.Builder<Message>()
                    .setLifecycleOwner(this)
                    .setQuery(query, Message::class.java)
                    .build()

            adapter = object : FirestoreMessageAdapter(options) {
                override fun getItemViewType(position: Int): Int =
                    if (uid == getItem(position).senderid) R.layout.item_message_from
                    else R.layout.item_message_to

                override fun onDataChanged() {
                    txtEmpty.isVisible = itemCount == 0
                }
            }
            adapter.setActions(object : MessageActions {
                override fun setImage(id: String, imageView: ImageView) {
                    loadFirebaseImage(imageView, storage.getMessagePhoto(id))
                }
            })

            recyclerMessages.apply {
                addOnLayoutChangeListener(listener)
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = adapter
            }

            lifecycleScope.launch(Dispatchers.IO) {
                updateChat(chat)
            }

            inputMessage.setEndIconOnClickListener {
                keyboard.hide(this)

                val text = txtMessage.text.toString()

                if (uri != Uri.EMPTY) uploadMessagePhoto(chat, text, UUID.randomUUID().toString())
                else {
                    val date = Timestamp.now()
                    val message = Message(
                        text,
                        chat.id,
                        chat.uid[args.senderIndex],
                        chat.uid[args.receiverIndex],
                        chat.username[args.senderIndex],
                        chat.username[args.receiverIndex],
                        null,
                        date
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