package com.vt.shoppet.firebase

import com.google.firebase.auth.AuthResult
import com.google.firebase.iid.InstanceIdResult

interface AuthRepo {
    suspend fun signIn(email: String, password: String): AuthResult

    suspend fun createUser(email: String, password: String): AuthResult

    suspend fun verifyEmail(): Void?

    suspend fun resetPassword(email: String): Void?

    suspend fun instanceId(): InstanceIdResult

    fun isLoggedIn(): Boolean

    fun isUserVerified(): Boolean

    fun email(): String

    fun signOut()

    fun uid(): String

    fun deleteInstanceId()
}