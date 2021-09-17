package com.vt.shoppet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.auth.AuthResult
import com.vt.shoppet.repo.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: AuthRepo) : ViewModel() {

    fun signIn(email: String, password: String): LiveData<Result<AuthResult>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                auth.signIn(email, password)
            }
            emit(result)
        }

    fun createUser(email: String, password: String): LiveData<Result<AuthResult>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                auth.createUser(email, password)
            }
            emit(result)
        }

    fun verifyEmail(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                auth.verifyEmail()
            }
            emit(result)
        }

    fun resetPassword(email: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                auth.resetPassword(email)
            }
            emit(result)
        }

    fun getToken(): LiveData<Result<String>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                auth.getToken()
            }
            emit(result)
        }

    fun isLoggedIn() = auth.isLoggedIn()

    fun isUserVerified() = auth.isUserVerified()

    fun email() = auth.email()

    fun signOut() = auth.signOut()

    fun uid() = auth.uid()

    fun deleteToken() = auth.deleteToken()

}