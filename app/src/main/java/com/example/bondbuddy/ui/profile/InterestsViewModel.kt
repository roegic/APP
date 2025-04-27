package com.example.bondbuddy.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.repo.UserRepo
import com.example.bondbuddy.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(val userRepo: UserRepo) : ViewModel() {

    private val _tags = MutableLiveData<List<String>?>()
    val allInterestsTags: LiveData<List<String>?> = _tags

    private val _getInterestsState = MutableStateFlow<Result<List<Interest>>>(Result.Loading())
    val getInterestsState: StateFlow<Result<List<Interest>>> = _getInterestsState.asStateFlow()

    private val _saveInterestState = MutableStateFlow<Result<SimpleResponse>?>(null)
    val saveInterestState: StateFlow<Result<SimpleResponse>?> = _saveInterestState.asStateFlow()

    private var isInitialLoadInterestsDone = false

    fun loadTags() {
        viewModelScope.launch {
            try {
                val result = userRepo.getAllInterestsTags()
                when (result) {
                    is Result.Success -> {
                        _tags.value = result.data
                    }

                    is Result.Error -> {
                        _tags.value = null
                    }

                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                _tags.value = null
            }
        }
    }

    fun getCurrentUserInterests() {
        if (!isInitialLoadInterestsDone || _getInterestsState.value is Result.Error) {
            viewModelScope.launch {
                _getInterestsState.value = Result.Loading()
                val result = userRepo.getUserInterests()
                _getInterestsState.value = result
                if (result is Result.Success) {
                    isInitialLoadInterestsDone = true
                }
            }
        }
    }

    fun saveServerCurrentUserInterests(userInterests: List<Interest>) {
        viewModelScope.launch {
            _saveInterestState.value = Result.Loading()
            val saveResult = userRepo.updateUserInterests(userInterests)
            _saveInterestState.value = saveResult

            if (saveResult is Result.Success) {
                isInitialLoadInterestsDone = false
                getCurrentUserInterests()
            }
        }
    }

    fun clearSaveState() {
        isInitialLoadInterestsDone = false
        _saveInterestState.value = null
    }
}