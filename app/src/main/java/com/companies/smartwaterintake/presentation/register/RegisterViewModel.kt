package com.companies.smartwaterintake.presentation.register

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companies.smartwaterintake.domain.service.AccountService
import com.companies.smartwaterintake.presentation.navigation.Graph
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val accountService: AccountService,
) : ViewModel() {

    var uiState = mutableStateOf(RegisterUiState())
    private val email get() = uiState.value.email
    private val password get() = uiState.value.password
    private val confirmPassword get() = uiState.value.confirmPassword
    private val username get() = uiState.value.username
    fun onEmailChange(newValue: String) {
        uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        uiState.value = uiState.value.copy(password = newValue)
    }

    fun onConfirmPassword(newValue: String) {
        uiState.value = uiState.value.copy(confirmPassword = newValue)
    }

    fun onUsernameChanged(newValue: String) {
        uiState.value = uiState.value.copy(username = newValue)
    }

    fun onRegisterClick(navigate: (String) -> Unit, context: Context) {
        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isBlank()) {
            Toast.makeText(context, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Please Enter Your data.", Toast.LENGTH_SHORT).show()
        }
        viewModelScope.launch {
            accountService.createAccount(
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                username = username
            ) { error ->
                if (error == null) {
                    linkRegisterAccount()
                    navigate(Graph.Home)
                } else {
                    Toast.makeText(
                        context,
                        "The email address is already in use.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun linkRegisterAccount() {
        viewModelScope.launch {
            accountService.registerAccount(email, password) { error ->
                if (error != null) {
                    Log.d("Link", "linkRegisterAccount: link Account")
                }
            }
        }
    }
}
