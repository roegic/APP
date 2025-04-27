package com.example.bondbuddy.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bondbuddy.data.remote.models.LoginRequest
import com.example.bondbuddy.data.remote.models.RegisterRequest
import com.example.bondbuddy.data.remote.models.User
import com.example.bondbuddy.repo.UserRepo
import com.example.bondbuddy.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserAuthViewModel @Inject constructor(val userRepo: UserRepo) : ViewModel() {

    private val _registerState = MutableSharedFlow<Result<String>>()
    val registerState: SharedFlow<Result<String>> = _registerState

    private val _loginState = MutableSharedFlow<Result<String>>()
    val loginState: SharedFlow<Result<String>> = _loginState


    fun registerUser(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) = viewModelScope.launch {
        _registerState.emit(Result.Loading())

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || password != confirmPassword) {
            _registerState.emit(Result.Error("Some Fields are empty"))
            return@launch
        }

        val newUser = RegisterRequest(
            firstName,
            lastName,
            email,
            username,
            password
        )
        _registerState.emit(userRepo.createUser(newUser))
    }

    fun loginUser(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _loginState.emit(Result.Loading())

        if (email.isEmpty() || password.isEmpty()) {
            _loginState.emit(Result.Error("Some Fields are empty"))
            return@launch
        }

        val user = LoginRequest(
            email,
            password
        )
        _loginState.emit(userRepo.login(user))
    }

    fun logout() = viewModelScope.launch {
        val result = userRepo.logout()
    }
}