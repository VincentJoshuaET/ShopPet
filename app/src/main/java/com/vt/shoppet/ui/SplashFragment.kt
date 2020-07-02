package com.vt.shoppet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vt.shoppet.R
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject
    lateinit var auth: AuthRepo

    private val viewModel: DataViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.isLoggedIn()) {
            if (auth.isUserVerified()) {
                viewModel.setUserLiveData(auth.uid())
                viewModel.setPetLiveData()
                findNavController().navigate(R.id.action_splash_to_home)
            } else {
                auth.signOut()
                findNavController().navigate(R.id.action_splash_to_auth)
            }
        } else findNavController().navigate(R.id.action_splash_to_auth)
    }

}