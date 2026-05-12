package com.umc.edison.presentation.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.crashlytics.ktx.crashlytics
import com.umc.edison.BuildConfig
import com.umc.edison.R
import com.google.firebase.ktx.Firebase
import com.umc.edison.common.logging.AppLogger
import com.umc.edison.common.logging.UserContext
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.usecase.user.GoogleLoginUseCase
import com.umc.edison.domain.usecase.sync.SyncServerDataToLocalUseCase
import com.umc.edison.domain.usecase.sync.SyncLocalDataToServerUseCase
import com.umc.edison.presentation.model.toPresentation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleLoginHelper @Inject constructor(
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val syncLocalDataToServerUseCase: SyncLocalDataToServerUseCase,
    private val syncServerDataToLocalUseCase: SyncServerDataToLocalUseCase,
    private val userContext: UserContext,
) {
    private val coroutineScope = MainScope()

    fun signInWithGoogle(
        context: Context,
        onResult: (GoogleLoginState) -> Unit
    ) {
        val signInWithGoogleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.firebase_config_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        coroutineScope.launch {
            try {
                onResult(GoogleLoginState.Loading)
                val response = credentialManager.getCredential(context, request)
                handleSignIn(response, onResult)
            } catch (e: GetCredentialException) {
                val errorMessage = when (e) {
                    is androidx.credentials.exceptions.GetCredentialCancellationException ->
                        GoogleLoginState.ERROR_MESSAGE_CANCELLED

                    else -> {
                        AppLogger.e(
                            "Google SignIn",
                            "GetCredentialException: ${e.javaClass.simpleName} - ${e.message}",
                            e
                        )
                        Firebase.crashlytics.setCustomKey("google_signin_phase", "get_credential")
                        Firebase.crashlytics.setCustomKey("google_signin_error_type", e.javaClass.simpleName)
                        Firebase.crashlytics.setCustomKey("google_signin_error_message", "A credential error occurred.")
                        GoogleLoginState.ERROR_MESSAGE_UNKNOWN
                    }
                }
                onResult(GoogleLoginState.Failure(errorMessage))
            }
        }
    }

    private fun handleSignIn(
        response: GetCredentialResponse,
        onResult: (GoogleLoginState) -> Unit
    ) {
        val credential = response.credential

        when (credential) {
            is GoogleIdTokenCredential -> {
                val idToken = credential.idToken
                if (BuildConfig.DEBUG){
                    AppLogger.d("Google SignIn", "ID Token: $idToken")
                }

                sendIdTokenToServer(idToken, onResult)
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        if (BuildConfig.DEBUG){
                            AppLogger.d("Google SignIn", "ID Token (CustomCredential): $idToken")
                        }
                        sendIdTokenToServer(idToken, onResult)
                    } catch (e: Exception) {
                        AppLogger.e("Google SignIn", "Received an invalid Google ID token response", e)
                        onResult(GoogleLoginState.Failure(GoogleLoginState.ERROR_MESSAGE_INVALID_TOKEN))
                    }
                } else {
                    AppLogger.w("Google SignIn", "CustomCredential type mismatch: ${credential.type}")
                    onResult(GoogleLoginState.Failure())
                }
            }

            else -> {
                AppLogger.w("Google SignIn", "Unknown credential type: ${credential.javaClass.simpleName}")
                onResult(GoogleLoginState.Failure())
            }
        }
    }

    private fun sendIdTokenToServer(
        idToken: String,
        onResult: (GoogleLoginState) -> Unit
    ) {
        coroutineScope.launch {
            googleLoginUseCase(idToken).collect { result ->
                when (result) {
                    is DataResource.Success -> {
                        onResult(GoogleLoginState.Success(result.data.toPresentation()))
                        // Crashlytics user 식별자 설정
                        result.data.id?.let { userContext.setAccountId(it.toString()) }

                        try {
                            syncLocalDataToServerUseCase()
                        } catch (e: Throwable) {
                            AppLogger.e("Init sync local to server data", "Failed to sync data", e)
                        }

                        try {
                            syncServerDataToLocalUseCase()
                        } catch (e: Throwable) {
                            AppLogger.e("Init sync server to local data", "Failed to sync data", e)
                        }
                    }

                    is DataResource.Error -> {
                        val t = result.throwable
                        AppLogger.e("Google SignIn", "로그인 실패", t)
                        val errorCode = (t as? HttpException)?.let { exception ->
                            exception.response()?.errorBody()?.string()?.also { errorBody ->
                                AppLogger.e("Google SignIn", "errorBody: $errorBody")
                            }?.let { errorBody ->
                                runCatching {
                                    JSONObject(errorBody).optString("code")
                                        .takeIf { it.isNotEmpty() }
                                }
                                    .onFailure { AppLogger.e("Google SignIn", "에러 바디 파싱 실패", it) }
                                    .getOrNull()
                            }
                        }
                        when (errorCode) {
                            GoogleLoginState.ERROR_CODE_MEMBER_NOT_FOUND -> {
                                onResult(GoogleLoginState.MemberNotFound(idToken))
                            }

                            else -> {
                                onResult(GoogleLoginState.Failure(GoogleLoginState.ERROR_MESSAGE_LOGIN_FAILED))
                            }
                        }
                    }

                    is DataResource.Loading -> {
                        onResult(GoogleLoginState.Loading)
                    }
                }
            }
        }
    }
}
