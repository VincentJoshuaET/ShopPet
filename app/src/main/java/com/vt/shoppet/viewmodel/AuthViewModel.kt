package com.vt.shoppet.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.auth.AuthResult
import com.google.firebase.iid.InstanceIdResult
import com.vt.shoppet.model.Result
import com.vt.shoppet.repo.AuthRepo
import kotlinx.coroutines.Dispatchers

class AuthViewModel @ViewModelInject constructor(
    private val auth: AuthRepo
) : ViewModel() {

    fun signIn(email: String, password: String): LiveData<Result<AuthResult>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(auth.signIn(email, password)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun createUser(email: String, password: String): LiveData<Result<AuthResult>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(auth.createUser(email, password)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun verifyEmail(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(auth.verifyEmail()))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun resetPassword(email: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(auth.resetPassword(email)))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun instanceId(): LiveData<Result<InstanceIdResult>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                emit(Result.Success(auth.instanceId()))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun isLoggedIn() = auth.isLoggedIn()

    fun isUserVerified() = auth.isUserVerified()

    fun email() = auth.email()

    fun signOut() = auth.signOut()

    fun uid() = auth.uid()

    fun deleteInstanceId() = auth.deleteInstanceId()

}