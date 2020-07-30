package com.vt.shoppet.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.auth.AuthResult
import com.google.firebase.iid.InstanceIdResult
import com.vt.shoppet.repo.AuthRepo
import kotlinx.coroutines.Dispatchers

class AuthViewModel @ViewModelInject constructor(
    private val auth: AuthRepo
) : ViewModel() {

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

    fun instanceId(): LiveData<Result<InstanceIdResult>> =
        liveData(Dispatchers.IO) {
            val result = runCatching {
                auth.instanceId()
            }
            emit(result)
        }

    fun isLoggedIn() = auth.isLoggedIn()

    fun isUserVerified() = auth.isUserVerified()

    fun email() = auth.email()

    fun signOut() = auth.signOut()

    fun uid() = auth.uid()

    fun deleteInstanceId() = auth.deleteInstanceId()

}