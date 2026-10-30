package com.galm.monitor.auth

import android.content.Context
import android.content.Intent
import com.galm.monitor.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

/**
 * Handles Google Sign-In using the official Google Identity Services.
 *
 * Scopes used: basic profile + email (minimal necessary).
 * No password is ever handled or stored.
 *
 * LIMITATION:
 * After successful sign-in we only receive the account identity.
 * Google does NOT return login history, IP addresses, device lists
 * or security events through the Sign-In API.
 */
class GoogleAuthManager(private val context: Context) {

    private val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestId()
        .requestProfile()
        // requestIdToken is needed if you later want to verify on a backend
        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .build()

    val signInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = signInClient.signInIntent

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun handleSignInResult(task: Task<GoogleSignInAccount>): Result<GoogleSignInAccount> {
        return try {
            val account = task.getResult(ApiException::class.java)
            Result.success(account)
        } catch (e: ApiException) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        signInClient.signOut().await()
    }

    suspend fun revokeAccess() {
        signInClient.revokeAccess().await()
    }
}
