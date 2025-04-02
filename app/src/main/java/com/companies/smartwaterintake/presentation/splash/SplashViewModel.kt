package com.companies.smartwaterintake.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companies.smartwaterintake.domain.service.AccountService
import com.companies.smartwaterintake.presentation.navigation.Graph
import com.companies.smartwaterintake.presentation.navigation.Login
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    fun onAppStart(navigate: (String) -> Unit) {
        viewModelScope.launch {
          if (accountService.hasUser) {
                accountService.getUserId()
                navigate(Graph.Home)
            } else {
                navigate(Login)
            }
        }
    }
}