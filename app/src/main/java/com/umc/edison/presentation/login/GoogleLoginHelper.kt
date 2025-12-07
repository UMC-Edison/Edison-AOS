package com.umc.edison.presentation.login

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.umc.edison.R
import com.umc.edison.domain.DataResource
import com.umc.edison.domain.usecase.user.GoogleLoginUseCase
import com.umc.edison.domain.usecase.sync.SyncServerDataToLocalUseCase
import com.umc.edison.domain.usecase.sync.SyncLocalDataToServerUseCase
import com.umc.edison.presentation.model.UserModel
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
) {
    private val coroutineScope = MainScope()

    fun signInWithGoogle(
        context: Context,
        onSuccess: (UserModel) -> Unit,
        onMemberNotFound: (String) -> Unit,
        onFailure: (String) -> Unit,
        onLoading: (Boolean) -> Unit
    ) {
        val signInWithGoogleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.google_cloud_server_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        coroutineScope.launch {
            try {
                onLoading(true)
                val response: GetCredentialResponse =
                    credentialManager.getCredential(context, request)
                handleSignIn(response, onSuccess, onMemberNotFound, onFailure, onLoading)
            } catch (e: GetCredentialException) {
                onLoading(false)
                Log.e("Google SignIn", "로그인 실패: ${e.message}", e)

                when (e) {
                    is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                        onFailure("사용자가 로그인 창을 닫았습니다.")
                    }

                    else -> {
                        onFailure("알 수 없는 로그인 오류 발생")
                    }
                }
            }
        }
    }

    private fun handleSignIn(
        response: GetCredentialResponse,
        onSuccess: (UserModel) -> Unit,
        onMemberNotFound: (String) -> Unit,
        onFailure: (String) -> Unit,
        onLoading: (Boolean) -> Unit
    ) {
        val credential = response.credential

        when (credential) {
            is GoogleIdTokenCredential -> {
                val idToken = credential.idToken
                Log.d("Google SignIn", "ID Token: $idToken")

                sendIdTokenToServer(idToken, onSuccess, onMemberNotFound, onFailure, onLoading)
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d("Google SignIn", "ID Token (CustomCredential): $idToken")

                        sendIdTokenToServer(idToken, onSuccess, onMemberNotFound, onFailure, onLoading)
                    } catch (e: Exception) {
                        Log.e("Google SignIn", "Received an invalid Google ID token response", e)
                        onFailure("잘못된 ID Token 응답을 받았습니다.")
                    }
                } else {
                    onFailure("Unexpected type of credential")
                }
            }

            else -> {
                onFailure("Unexpected type of credential")
            }
        }
    }

    private fun sendIdTokenToServer(
        idToken: String,
        onSuccess: (UserModel) -> Unit,
        onMemberNotFound: (String) -> Unit,
        onFailure: (String) -> Unit,
        onLoading: (Boolean) -> Unit
    ) {
        coroutineScope.launch {
            googleLoginUseCase(idToken).collect { result ->
                when (result) {
                    is DataResource.Success -> {
                        Log.d("Google SignIn", "서버 로그인 성공: ${result.data}")
                        onLoading(false)
                        onSuccess(result.data.toPresentation())

                        try {
                            syncLocalDataToServerUseCase()
                        } catch (e: Throwable) {
                            Log.e("Init sync local to server data", "Failed to sync data", e)
                        }

                        try {
                            syncServerDataToLocalUseCase()
                        } catch (e: Throwable) {
                            Log.e("Init sync server to local data", "Failed to sync data", e)
                        }
                    }

                    is DataResource.Error -> {
                        onLoading(false)

                        val t = result.throwable
                        Log.e("Google SignIn", "로그인 실패", t)

                        var code: String? = null
                        if (t is HttpException) {
                            val errorBody = t.response()?.errorBody()?.string()
                            Log.e("Google SignIn", "errorBody: $errorBody")

                            try {
                                val json = JSONObject(errorBody ?: "")
                                code = json.optString("code", null)
                            } catch (e: Exception) {
                                Log.e("Google SignIn", "에러 바디 파싱 실패", e)
                            }
                        }

                        when (code) {
                            "MEMBER4001" -> {
                                onMemberNotFound(idToken)
                            }
                            else -> {
                                onFailure("LOGIN_ERROR")
                            }
                        }
                    }

                    is DataResource.Loading -> {
                        onLoading(true)
                    }
                }
            }
        }
    }
}
