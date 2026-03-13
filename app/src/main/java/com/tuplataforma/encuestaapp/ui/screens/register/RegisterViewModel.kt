package com.tuplataforma.encuestaapp.ui.screens.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RegisterUiState(
    // Datos Personales
    val name: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    
    // Datos de Cuenta
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    
    val step: Int = 1, // 1: Datos Personales, 2: Cuenta
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    private val auth = FirebaseAuth.getInstance()

    // Manejo de Datos Personales
    fun onNameChange(name: String) = updateState { it.copy(name = name) }
    fun onLastNameChange(lastName: String) = updateState { it.copy(lastName = lastName) }
    fun onBirthDateChange(date: String) = updateState { it.copy(birthDate = date) }
    fun onGenderChange(gender: String) = updateState { it.copy(gender = gender) }

    // Manejo de Cuenta
    fun onEmailChange(email: String) = updateState { it.copy(email = email) }
    fun onPasswordChange(password: String) = updateState { it.copy(password = password) }
    fun onConfirmPasswordChange(confirm: String) = updateState { it.copy(confirmPassword = confirm) }

    fun nextStep() {
        if (uiState.name.isBlank() || uiState.lastName.isBlank() || uiState.birthDate.isBlank() || uiState.gender.isBlank()) {
            updateState { it.copy(errorMessage = "Completa todos tus datos personales") }
            return
        }
        updateState { it.copy(step = 2, errorMessage = null) }
    }

    fun previousStep() {
        updateState { it.copy(step = 1, errorMessage = null) }
    }

    fun register() {
        val email = uiState.email.trim()
        val password = uiState.password
        val confirmPassword = uiState.confirmPassword

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            updateState { it.copy(errorMessage = "Completa todos los campos") }
            return
        }

        if (password != confirmPassword) {
            updateState { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                // Guardar el nombre en el perfil de Firebase
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("${uiState.name} ${uiState.lastName}")
                    .build()
                
                result.user?.updateProfile(profileUpdates)?.await()
                
                updateState { it.copy(isLoading = false, isRegisterSuccess = true) }
            } catch (e: Exception) {
                updateState { it.copy(
                    isLoading = false, 
                    errorMessage = e.localizedMessage ?: "Error al crear la cuenta"
                ) }
            }
        }
    }

    private fun updateState(update: (RegisterUiState) -> RegisterUiState) {
        uiState = update(uiState)
    }
}
