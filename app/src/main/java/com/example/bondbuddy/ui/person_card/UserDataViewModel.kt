package com.example.bondbuddy.ui.person_card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.data.remote.models.Language
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.repo.UserRepo
import com.example.bondbuddy.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDataViewModel @Inject constructor(private val userRepo: UserRepo) : ViewModel() {

    private val _getInterestsState = MutableSharedFlow<Result<List<Interest>>>()
    val getInterestsState: SharedFlow<Result<List<Interest>>> = _getInterestsState

    private val _getSocialsState = MutableSharedFlow<Result<List<SocialMedia>>>()
    val getSocialsState: SharedFlow<Result<List<SocialMedia>>> = _getSocialsState

    private val _getLanguagesState = MutableSharedFlow<Result<List<Language>>>()
    val getLanguagesState: SharedFlow<Result<List<Language>>> = _getLanguagesState

    private val _userInfoState = MutableSharedFlow<Result<UserInfo>>()
    val userState: SharedFlow<Result<UserInfo>> = _userInfoState

    fun getCurrentUserInterests(userId: String) = viewModelScope.launch {
        _getInterestsState.emit(userRepo.getUserInterestsById(userId))
    }

    fun getCurrentUserSocials(userId: String) = viewModelScope.launch {
        _getSocialsState.emit(userRepo.getUserSocialsById(userId))
    }

    fun getCurrentUserLanguages(userId: String) = viewModelScope.launch {
        _getLanguagesState.emit(userRepo.getUserLanguagesById(userId))
    }

    fun getCurrentUserInfo(userId: String) = viewModelScope.launch {
        _userInfoState.emit(userRepo.getUserInfoById(userId))
    }
}