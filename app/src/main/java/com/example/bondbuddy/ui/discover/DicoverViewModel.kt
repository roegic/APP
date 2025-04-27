package com.example.bondbuddy.ui.discover

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
class DiscoverViewModel @Inject constructor(private val userRepo: UserRepo) : ViewModel() {
    private val _displayedUsersState = MutableStateFlow<Result<List<UserInfo>>>(Result.Loading())
    val displayedUsersState: StateFlow<Result<List<UserInfo>>> = _displayedUsersState.asStateFlow()

    private var isInitialLoadDone = false

    fun getCurrentUserRecommendations() {
        if (!isInitialLoadDone || _displayedUsersState.value !is Result.Success) {
            viewModelScope.launch {
                _displayedUsersState.value = Result.Loading()
                val result = userRepo.getUserDefaultRecommendations()
                _displayedUsersState.value = result
                if (result is Result.Success) {
                    isInitialLoadDone = true
                }
            }
        }
    }

    fun getFilteredRecommendations(
        selectedInterests: List<String>?,
        minAge: String?,
        maxAge: String?,
        city: String?,
        country: String?,
        languages: List<String>?
    ) {
        viewModelScope.launch {
            _displayedUsersState.value = Result.Loading()
            val result = userRepo.getFilteredUserRecommendations(
                selectedInterests,
                minAge,
                maxAge,
                city,
                country,
                languages
            )
            _displayedUsersState.value = result
        }
    }

    fun resetToDefaultRecommendations() {
        isInitialLoadDone = false
        getCurrentUserRecommendations()
    }
}