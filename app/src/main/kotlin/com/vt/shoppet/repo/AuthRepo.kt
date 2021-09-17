package com.vt.shoppet.repo

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging
) {

    suspend fun signIn(email: String, password: String): AuthResult =
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun createUser(email: String, password: String): AuthResult =
        auth.createUserWithEmailAndPassword(email, password).await()

    suspend fun verifyEmail(): Void? =
        auth.currentUser?.sendEmailVerification()?.await()

    suspend fun resetPassword(email: String): Void? =
        auth.sendPasswordResetEmail(email).await()

    suspend fun getToken(): String =
        messaging.token.await()

    fun isLoggedIn() = auth.currentUser != null

    fun isUserVerified() = auth.currentUser?.isEmailVerified != null

    fun email() = auth.currentUser?.email

    fun signOut() = auth.signOut()

    fun uid() = auth.uid

    fun deleteToken() {
        messaging.deleteToken()
    }

}