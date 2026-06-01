package com.ud.finalproyect.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Por favor completa todos los campos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    _user.value = authResult.user
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    _error.value = "Error al iniciar sesión: ${e.localizedMessage}"
                    _isLoading.value = false
                }
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Por favor completa todos los campos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    _user.value = authResult.user
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    _error.value = "Error al registrarse: ${e.localizedMessage}"
                    _isLoading.value = false
                }
        }
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetSignInWithGoogleOption
                    .Builder(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(result.credential.data)

                val firebaseCredential = GoogleAuthProvider
                    .getCredential(googleIdTokenCredential.idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { authResult ->
                        _user.value = authResult.user
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Error Firebase: ${e.localizedMessage}"
                        _isLoading.value = false
                    }

            } catch (e: GetCredentialException) {
                _error.value = "Error de Google: ${e.message}"
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
