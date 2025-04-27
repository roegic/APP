package com.example.bondbuddy.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.repo.UserRepo
import com.example.bondbuddy.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(private val userRepo: UserRepo) : ViewModel() {

    private val _followerListState = MutableStateFlow<Result<List<UserInfo>>>(Result.Loading())
    val followerListState: StateFlow<Result<List<UserInfo>>> = _followerListState.asStateFlow()

    fun unfollowUser(userId: Int, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = userRepo.unfollowUser(userId)

                when (result) {
                    is Result.Success -> {
                        onSuccess(result.data?.message ?: "Success!")
                        getUserFollowerList()
                    }

                    is Result.Error -> {
                        onError(result.errorMessage ?: "Неизвестная ошибка")
                    }

                    is Result.Loading -> {
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Ошибка подключения: ${e.message}")
            }
        }
    }

    fun followUser(userId: Int, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = userRepo.followUser(userId)

                when (result) {
                    is Result.Success -> {
                        onSuccess(result.data?.message ?: "Подписка выполнена!")
                    }

                    is Result.Error -> {
                        onError(result.errorMessage ?: "Неизвестная ошибка")
                    }

                    is Result.Loading -> {
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                onError("Ошибка подключения: ${e.message}")
            }
        }
    }

    fun getUserFollowerList() = viewModelScope.launch {
        _followerListState.value = Result.Loading()
        val result = userRepo.getUserFollowerList()
        _followerListState.value = result
    }

    fun refreshFollowerList() = viewModelScope.launch {
        _followerListState.value = Result.Loading()
        val result = userRepo.getUserFollowerList()
        _followerListState.value = result
    }
}