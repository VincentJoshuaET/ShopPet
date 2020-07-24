package com.vt.shoppet.impl

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.vt.shoppet.repo.AuthRepo
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepoImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val instanceId: FirebaseInstanceId
) : AuthRepo {

    override suspend fun signIn(email: String, password: String): AuthResult =
        auth.signInWithEmailAndPassword(email, password).await()

    override suspend fun createUser(email: String, password: String): AuthResult =
        auth.createUserWithEmailAndPassword(email, password).await()

    override suspend fun verifyEmail(): Void? =
        auth.currentUser?.sendEmailVerification()?.await()

    override suspend fun resetPassword(email: String): Void? =
        auth.sendPasswordResetEmail(email).await()

    override suspend fun instanceId(): InstanceIdResult =
        instanceId.instanceId.await()

    override fun isLoggedIn() = auth.currentUser != null

    override fun isUserVerified() = auth.currentUser?.isEmailVerified != null

    override fun email() = auth.currentUser?.email as String

    override fun signOut() = auth.signOut()

    override fun uid() = auth.uid as String

    override fun deleteInstanceId() = instanceId.deleteInstanceId()

}