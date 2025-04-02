package com.companies.smartwaterintake.presentation.register

data class RegisterUiState(
    var username : String = "",
    var email :String = "",
    var password : String = "",
    var confirmPassword : String = ""
)