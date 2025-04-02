package com.companies.smartwaterintake.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companies.smartwaterintake.domain.service.AccountService
import com.companies.smartwaterintake.presentation.navigation.Graph
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow() // Safer to expose immutable flow

    init {
        getUsername()
    }

    fun getUsername() {
        viewModelScope.launch {
            val uid = accountService.getUserId()
            accountService.fetchUsernameFromFirestore(uid = uid) { fetchedUsername ->
                if (!fetchedUsername.isNullOrEmpty()) {
                    viewModelScope.launch {
                        _username.emit(fetchedUsername) // 🔥 Use emit() instead of direct assignment
                    }
                    Log.d("Username", "getUsername: $fetchedUsername")
                }
            }
        }
    }

    fun changeUsername(newUsername: String) {
        viewModelScope.launch {
            accountService.updateUsername(newUsername) { error ->
                if (error == null) {
                    viewModelScope.launch {
                        _username.emit(newUsername) // 🔥 Update state after Firestore update
                    }
                } else {
                    Log.e("Username", "Failed to update: $error")
                }
            }
        }
    }

    fun logout(navigate: (String) -> Unit) {
        viewModelScope.launch {
            try {
                accountService.logout(
                    onSuccess = { navigate(Graph.Authentication) },
                    onError = { Log.d("TAG", "logout: error") }
                )
            } catch (e: Exception) {
                Log.d("error", "logout: $e")
            }
        }
    }
}