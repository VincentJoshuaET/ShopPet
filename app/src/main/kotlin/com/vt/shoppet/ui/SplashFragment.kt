package com.vt.shoppet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vt.shoppet.R
import com.vt.shoppet.viewmodel.AuthViewModel
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val auth: AuthViewModel by activityViewModels()
    private val dataViewModel: DataViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.isLoggedIn()) {
            if (auth.isUserVerified()) {
                dataViewModel.initFirebaseData()
                findNavController().navigate(R.id.action_splash_to_home)
            } else {
                auth.signOut()
                findNavController().navigate(R.id.action_splash_to_auth)
            }
        } else findNavController().navigate(R.id.action_splash_to_auth)
    }

}