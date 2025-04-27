package com.example.bondbuddy.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.Language
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bondbuddy.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class LanguagesViewModel @Inject constructor(val userRepo: UserRepo) : ViewModel() {

    private val _languages = MutableStateFlow<List<String>?>(null)
    val languages: StateFlow<List<String>?> = _languages.asStateFlow()

    private val _userLanguagesState = MutableStateFlow<Result<List<Language>>>(Result.Loading())
    val userLanguagesState: StateFlow<Result<List<Language>>> = _userLanguagesState.asStateFlow()

    private val _updateLanguagesResult = MutableStateFlow<Result<SimpleResponse>?>(null)
    val updateLanguagesResult: StateFlow<Result<SimpleResponse>?> =
        _updateLanguagesResult.asStateFlow()

    private var isInitialLoadUserLanguagesDone = false

    fun loadLanguages() {
        if (_languages.value == null) {
            viewModelScope.launch {
                try {
                    val result = userRepo.getSupportedLanguagesList()
                    when (result) {
                        is Result.Success -> {
                            _languages.value = result.data?.map { it.nativeName }
                        }

                        is Result.Error -> {
                            _languages.value = null
                        }

                        is Result.Loading -> {}
                    }
                } catch (e: Exception) {
                    _languages.value = null
                }
            }
        }
    }

    fun updateUserLanguages(userLanguages: List<String>) {
        viewModelScope.launch {
            _updateLanguagesResult.value = Result.Loading()
            val saveResult = userRepo.updateUserLanguages(userLanguages)
            _updateLanguagesResult.value = saveResult

            if (saveResult is Result.Success) {
                isInitialLoadUserLanguagesDone = false
                getUserLanguages()
            }
        }
    }

    fun getUserLanguages() {
        if (!isInitialLoadUserLanguagesDone || _userLanguagesState.value is Result.Error) {
            viewModelScope.launch {
                _userLanguagesState.value = Result.Loading()
                val result = userRepo.getUserLanguages()
                _userLanguagesState.value = result
                if (result is Result.Success) {
                    isInitialLoadUserLanguagesDone = true
                }
            }
        }
    }

    fun clearUpdateState() {
        isInitialLoadUserLanguagesDone = false
        _updateLanguagesResult.value = null
    }
}