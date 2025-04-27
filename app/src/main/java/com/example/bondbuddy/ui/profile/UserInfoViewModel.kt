package com.example.bondbuddy.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.SimpleResponse
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
class UserInfoViewModel @Inject constructor(val userRepo: UserRepo) : ViewModel() {

    private val _userInfoState = MutableStateFlow<Result<UserInfo>>(Result.Loading())
    val userState: StateFlow<Result<UserInfo>> = _userInfoState.asStateFlow()

    private val _saveUserInfoState = MutableStateFlow<Result<SimpleResponse>?>(null)
    val saveUserInfoState: StateFlow<Result<SimpleResponse>?> = _saveUserInfoState.asStateFlow()

    private var isInitialLoadUserInfoDone = false

    fun getCurrentUserInfo() {
        if (!isInitialLoadUserInfoDone || _userInfoState.value is Result.Error) {
            viewModelScope.launch {
                _userInfoState.value = Result.Loading()
                val result = userRepo.getUserInfo()
                _userInfoState.value = result
                if (result is Result.Success) {
                    isInitialLoadUserInfoDone = true
                }
            }
        }
    }

    fun saveServerCurrentUserInfo(userInfo: UserInfo) {
        viewModelScope.launch {
            _saveUserInfoState.value = Result.Loading()
            val saveResult = userRepo.updateUserInfo(userInfo)
            _saveUserInfoState.value = saveResult

            if (saveResult is Result.Success) {
                isInitialLoadUserInfoDone = false
                getCurrentUserInfo()
            }
        }
    }

    fun clearSaveState() {
        isInitialLoadUserInfoDone = false
        _saveUserInfoState.value = null
    }
}