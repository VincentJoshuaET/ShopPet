package com.vt.shoppet.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.vt.shoppet.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val instanceId: FirebaseInstanceId
) {

    fun signIn(email: String, password: String): LiveData<Result<AuthResult>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                emit(Result.Success(result))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun createUser(email: String, password: String): LiveData<Result<AuthResult>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                emit(Result.Success(result))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun verifyEmail(): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = auth.currentUser?.sendEmailVerification()?.await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun resetPassword(email: String): LiveData<Result<Void?>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val task = auth.sendPasswordResetEmail(email).await()
                emit(Result.Success(task))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun instanceId(): LiveData<Result<InstanceIdResult>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading())
            try {
                val result = instanceId.instanceId.await()
                emit(Result.Success(result))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }

    fun isLoggedIn() = auth.currentUser != null

    fun isUserVerified() = auth.currentUser?.isEmailVerified != null

    fun email() = auth.currentUser?.email

    fun signOut() = auth.signOut()

    fun uid() = auth.uid as String

    fun deleteInstanceId() = instanceId.deleteInstanceId()

}