package com.example.bondbuddy.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bondbuddy.utils.Result

@HiltViewModel
class SocialsViewModel @Inject constructor(val userRepo: UserRepo) : ViewModel() {
    private val _getSocialsState = MutableStateFlow<Result<List<SocialMedia>>>(Result.Loading())
    val getSocialsState: StateFlow<Result<List<SocialMedia>>> = _getSocialsState.asStateFlow()

    private val _saveSocialsState = MutableStateFlow<Result<SimpleResponse>?>(null)
    val saveSocialsState: StateFlow<Result<SimpleResponse>?> = _saveSocialsState.asStateFlow()

    private var isInitialLoadSocialsDone = false

    fun getCurrentUserSocials() {
        if (!isInitialLoadSocialsDone || _getSocialsState.value is Result.Error) {
            viewModelScope.launch {
                _getSocialsState.value = Result.Loading()
                val result = userRepo.getUserSocials()
                _getSocialsState.value = result
                if (result is Result.Success) {
                    isInitialLoadSocialsDone = true
                }
            }
        }
    }

    fun saveServerCurrentUserSocials(userSocials: List<SocialMedia>) {
        viewModelScope.launch {
            _saveSocialsState.value = Result.Loading()
            val saveResult = userRepo.updateUserSocials(userSocials)
            _saveSocialsState.value = saveResult

            if (saveResult is Result.Success) {
                isInitialLoadSocialsDone = false
                getCurrentUserSocials()
            }
        }
    }

    fun clearSaveState() {
        isInitialLoadSocialsDone = false
        _saveSocialsState.value = null
    }
}