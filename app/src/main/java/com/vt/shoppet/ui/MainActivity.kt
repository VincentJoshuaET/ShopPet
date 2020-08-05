package com.vt.shoppet.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.ktx.toObject
import com.vt.shoppet.R
import com.vt.shoppet.databinding.ActivityMainBinding
import com.vt.shoppet.databinding.HeaderMainBinding
import com.vt.shoppet.model.Chat
import com.vt.shoppet.util.*
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import com.vt.shoppet.viewmodel.FirestoreViewModel
import com.vt.shoppet.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val auth: AuthViewModel by viewModels()
    private val firestore: FirestoreViewModel by viewModels()
    private val storage: StorageViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var navController: NavController
    lateinit var toolbar: MaterialToolbar

    fun instanceId() {
        auth.instanceId().observe(this) { result ->
            result.onSuccess { instanceIdResult ->
                signOut(instanceIdResult.token)
            }
            result.onFailure { exception ->
                binding.showActionSnackbar(exception) {
                    instanceId()
                }
            }
        }
    }

    private fun signOut(token: String) {
        firestore.removeToken(token).observe(this) { result ->
            result.onSuccess {
                dataViewModel.removeFirebaseData()
                auth.signOut()
                if (auth.isLoggedIn()) {
                    binding.showSnackbar(getString(R.string.txt_cannot_log_out))
                    dataViewModel.initFirebaseData()
                    firestore.addToken(token)
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        auth.deleteInstanceId()
                    }
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            result.onFailure { exception ->
                binding.showActionSnackbar(exception) {
                    signOut(token)
                }
            }
        }
    }

    private fun getChat(
        id: String,
        senderIndex: Int,
        receiverIndex: Int,
        senderUsername: String,
        onError: (View) -> Unit
    ) {
        firestore.getChat(id).observe(this@MainActivity) { result ->
            result.onSuccess { document ->
                val chat: Chat = document.toObject() ?: return@observe
                dataViewModel.setChat(chat)
                val arguments = bundleOf(
                    "senderIndex" to receiverIndex,
                    "receiverIndex" to senderIndex,
                    "username" to senderUsername
                )
                navController.navigate(R.id.fragment_conversation, arguments)
            }
            result.onFailure { exception ->
                binding.showActionSnackbar(exception, onError)
            }
        }
    }

    private fun readChatIntent(intent: Intent?) {
        intent?.let {
            val id = intent.getStringExtra("CHAT_ID") ?: return@let
            val senderIndex = intent.getStringExtra("SENDER_INDEX")?.toInt() ?: return@let
            val receiverIndex = intent.getStringExtra("RECIPIENT_INDEX")?.toInt() ?: return@let
            val senderUsername = intent.getStringExtra("SENDER_USERNAME") ?: return@let
            getChat(id, senderIndex, receiverIndex, senderUsername) {
                readChatIntent(intent)
            }
        }
    }

    private fun showChatSnackbar(intent: Intent?) {
        intent?.let {
            val id = intent.getStringExtra("CHAT_ID") ?: return@let
            val senderIndex = intent.getStringExtra("SENDER_INDEX")?.toIntOrNull() ?: return@let
            val receiverIndex =
                intent.getStringExtra("RECIPIENT_INDEX")?.toIntOrNull() ?: return@let
            val senderUsername = intent.getStringExtra("SENDER_USERNAME") ?: return@let
            if (dataViewModel.chat.value?.id == id && navController.currentDestination?.id == R.id.fragment_conversation) return@let
            binding.showActionSnackbar("$senderUsername messaged you") {
                getChat(id, senderIndex, receiverIndex, senderUsername) {
                    showChatSnackbar(intent)
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showChatSnackbar(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(receiver, IntentFilter("ACTION_CHAT"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        toolbar = binding.toolbar
        val bottomNavigationView = binding.bottomNavigationView
        val drawer = binding.drawer
        val navigationViewMain = binding.navigationViewMain
        val navigationView = binding.navigationView

        val header = navigationViewMain.getHeaderView(0)
        val headerBinding = HeaderMainBinding.bind(header)
        val imageUser = headerBinding.imageUser
        val txtName = headerBinding.txtName
        val txtUsername = headerBinding.txtUsername
        val txtEmail = headerBinding.txtEmail

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        val destinations = setOf(R.id.fragment_login, R.id.fragment_shop, R.id.fragment_chat)

        bottomNavigationView.setupWithNavController(navController)
        navigationViewMain.setupWithNavController(navController)
        navigationView.setupWithNavController(navController)
        toolbar.setupWithNavController(navController, AppBarConfiguration(destinations, drawer))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.menu_item_chat)
            val channel = NotificationChannel(name, name, NotificationManager.IMPORTANCE_HIGH)
            channel.description = name
            notificationManager.createNotificationChannel(channel)
        }

        if (savedInstanceState == null) readChatIntent(intent)
        notificationManager.cancelAll()

        dataViewModel.currentUser.observe(this) { user ->
            txtName.text = user.name
            txtUsername.text = user.username
            txtEmail.text = auth.email()
            val image =
                user.image ?: return@observe imageUser.setImageResource(R.drawable.ic_person)
            loadProfileImage(imageUser, storage.getUserPhoto(image))
        }

        dataViewModel.unread.observe(this) { unread ->
            if (unread == 0) bottomNavigationView.removeBadge(R.id.fragment_chat)
            else bottomNavigationView.getOrCreateBadge(R.id.fragment_chat).number = unread
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_splash -> binding.setupAuthView()
                R.id.fragment_login -> binding.setupAuthView()
                R.id.fragment_register -> binding.setupAuthView()
                R.id.fragment_forgot_password -> binding.setupAuthView()
                R.id.fragment_shop -> {
                    binding.setupHomeNavigationView()
                    toolbar.inflateMenu(R.menu.menu_shop)
                }
                R.id.fragment_chat -> binding.setupHomeNavigationView()
                R.id.fragment_profile -> binding.setupToolbar()
                R.id.fragment_starred -> binding.setupToolbar()
                R.id.fragment_own -> binding.setupToolbar()
                R.id.fragment_camera -> binding.setupToolbar()
                R.id.fragment_sell -> binding.setupToolbar()
                R.id.fragment_selected -> binding.setupToolbar()
                R.id.fragment_details -> binding.setupToolbar(R.menu.menu_edit)
                R.id.fragment_edit_pet -> binding.setupToolbar(R.menu.menu_edit)
                R.id.fragment_edit_profile -> binding.setupToolbar(R.menu.menu_edit)
                R.id.fragment_conversation -> binding.setupToolbar()
            }
        }
    }

}