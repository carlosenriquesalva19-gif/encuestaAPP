package com.tuplataforma.encuestaapp.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val userRole: String? = null
)

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null, successMessage = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null, successMessage = null)
    }

    fun loginWithEmail() {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid
                
                if (userId != null) {
                    val userDoc = db.collection("users").document(userId).get().await()
                    // Limpiamos el rol quitando espacios y pasando a minúsculas
                    val role = userDoc.getString("role")?.trim()?.lowercase() ?: "user"
                    uiState = uiState.copy(isLoading = false, isLoginSuccess = true, userRole = role)
                } else {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Error al obtener usuario")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun resetPassword() {
        val email = uiState.email.trim()
        if (email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Ingresa tu correo para restablecer la contraseña")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                auth.sendPasswordResetEmail(email).await()
                uiState = uiState.copy(
                    isLoading = false,
                    successMessage = "Se ha enviado un correo para restablecer tu contraseña"
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al enviar correo de recuperación"
                )
            }
        }
    }

    fun handleGoogleSignInResult(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            firebaseAuthWithGoogle(idToken)
        } else {
            uiState = uiState.copy(errorMessage = "Credencial de Google no válida")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(firebaseCredential).await()
                val userId = result.user?.uid

                if (userId != null) {
                    val userDoc = db.collection("users").document(userId).get().await()
                    
                    if (!userDoc.exists()) {
                        val newUser = hashMapOf(
                            "email" to result.user?.email,
                            "role" to "user",
                            "uid" to userId
                        )
                        db.collection("users").document(userId).set(newUser).await()
                        uiState = uiState.copy(isLoading = false, isLoginSuccess = true, userRole = "user")
                    } else {
                        val role = userDoc.getString("role")?.trim()?.lowercase() ?: "user"
                        uiState = uiState.copy(isLoading = false, isLoginSuccess = true, userRole = role)
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error con Google Sign-In"
                )
            }
        }
    }

    fun onGoogleSignInError(error: String) {
        uiState = uiState.copy(errorMessage = error)
    }
}
