package com.ertan.projecrmanagerapp.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleAuthHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    // Result wrapper: success returns the ID token, failure returns an error message
    sealed class SignInResult {
        data class Success(val idToken: String) : SignInResult()
        data class Failure(val message: String) : SignInResult()
    }

    suspend fun signIn(webClientId: String): SignInResult {
        return try {
            val option = GetSignInWithGoogleOption.Builder(webClientId).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            SignInResult.Success(googleIdTokenCredential.idToken)

        } catch (e: GetCredentialException) {
            android.util.Log.e("GOOGLE_AUTH", "Type: ${e.type}, Message: ${e.message}", e)
            SignInResult.Failure("Google sign-in failed: ${e.message}")
        } catch (e: Exception) {
            SignInResult.Failure("Unexpected error: ${e.message}")
        }
    }
}