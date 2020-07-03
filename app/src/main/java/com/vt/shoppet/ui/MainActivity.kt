package com.vt.shoppet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.vt.shoppet.R
import com.vt.shoppet.databinding.ActivityMainBinding
import com.vt.shoppet.databinding.HeaderMainBinding
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.StorageRepo
import com.vt.shoppet.util.loadProfileImage
import com.vt.shoppet.util.viewBinding
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val viewModel: DataViewModel by viewModels()

    @Inject
    lateinit var auth: AuthRepo

    @Inject
    lateinit var storage: StorageRepo

    lateinit var toolbar: MaterialToolbar

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var drawer: DrawerLayout

    private lateinit var appbar: AppBarLayout
    private lateinit var navigationViewMain: NavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var imageUser: ShapeableImageView
    private lateinit var txtName: TextView
    private lateinit var txtUsername: TextView
    private lateinit var txtEmail: TextView

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private fun bindViews() {
        appbar = binding.appbar
        bottomNavigationView = binding.bottomNavigationView
        drawer = binding.drawer
        navigationViewMain = binding.navigationViewMain
        navigationView = binding.navigationView
        toolbar = binding.toolbar

        val header = navigationViewMain.getHeaderView(0)
        val binding = HeaderMainBinding.bind(header)
        imageUser = binding.imageUser
        txtName = binding.txtName
        txtUsername = binding.txtUsername
        txtEmail = binding.txtEmail

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController

        val fragments =
            setOf(R.id.fragment_splash, R.id.fragment_login, R.id.fragment_shop, R.id.fragment_chat)

        bottomNavigationView.setupWithNavController(navController)
        navigationViewMain.setupWithNavController(navController)
        navigationView.setupWithNavController(navController)
        toolbar.setupWithNavController(navController, AppBarConfiguration(fragments, drawer))
    }

    private fun setupAuthView() {
        appbar.isVisible = false
        bottomNavigationView.isVisible = false
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.menu.clear()
    }

    private fun setupHomeNavigationView() {
        appbar.isVisible = true
        bottomNavigationView.isVisible = true
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toolbar.menu.clear()
    }

    private fun setupToolbar() {
        appbar.isVisible = true
        bottomNavigationView.isVisible = false
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.menu.clear()
    }

    fun restartActivity() {
        lifecycleScope.launch(Dispatchers.IO) {
            auth.deleteInstanceId()
        }
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()

        viewModel.getCurrentUser().observe(this) { user ->
            txtName.text = user.name
            txtUsername.text = user.username
            txtEmail.text = auth.email()

            val image = user.image
            if (image.isNotEmpty()) {
                loadProfileImage(imageUser, storage.getUserPhoto(image))
            } else imageUser.setImageResource(R.drawable.ic_person)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_splash -> setupAuthView()
                R.id.fragment_login -> setupAuthView()
                R.id.fragment_register -> setupAuthView()
                R.id.fragment_forgot_password -> setupAuthView()
                R.id.fragment_shop -> {
                    setupHomeNavigationView()
                    toolbar.inflateMenu(R.menu.menu_shop)
                }
                R.id.fragment_chat -> setupHomeNavigationView()
                R.id.fragment_profile -> setupToolbar()
                R.id.fragment_starred -> setupToolbar()
                R.id.fragment_own -> setupToolbar()
                R.id.fragment_camera -> setupToolbar()
                R.id.fragment_sell -> setupToolbar()
                R.id.fragment_selected -> setupToolbar()
                R.id.fragment_edit_profile -> {
                    setupToolbar()
                    toolbar.inflateMenu(R.menu.menu_edit_profile)
                }
            }
        }
    }

}