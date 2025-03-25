package com.companies.smartwaterintake.presentation.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companies.smartwaterintake.domain.service.AccountService
import com.companies.smartwaterintake.presentation.navigation.Graph
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService,
) : ViewModel() {
    var uiState = mutableStateOf(LoginUiState())
    private val email get() = uiState.value.email
    private val password get() = uiState.value.password


    fun onEmailChange(newValue: String) {
        this.uiState.value = uiState.value.copy(email = newValue)
    }

    fun onPasswordChange(newValue: String) {
        this.uiState.value = uiState.value.copy(password = newValue)
    }

    fun onLoginClick(openScreen: (String) -> Unit,context: Context) {
        viewModelScope.launch  {
            if(password.isEmpty()){
                Toast.makeText(context,"Please Enter Your Password",Toast.LENGTH_SHORT).show()
            }
            accountService.authenticate(email, password) { error ->
                if (error == null) {
                    linkAccount()
                    openScreen(Graph.Home)
                    Toast.makeText(context, "Authenticated Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = when (error) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email."
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                        else -> "Authentication failed. Please check your credentials."
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun linkAccount() {
        viewModelScope.launch {
            accountService.linkAccount(email, password) { error ->
                if (error != null) {
                    Log.d("TAG", "linkAccount: Account Link")
                }
            }
        }
    }

}