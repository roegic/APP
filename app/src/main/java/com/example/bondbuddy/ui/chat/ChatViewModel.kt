package com.example.bondbuddy.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondbuddy.data.remote.models.ChatUserInfo
import com.example.bondbuddy.data.remote.models.Message
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.bondbuddy.utils.Result
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userRepo: UserRepo
) : ViewModel() {

    private val _messagesState = MutableStateFlow<Result<List<Message>>>(Result.Loading())
    val messagesState: StateFlow<Result<List<Message>>> = _messagesState.asStateFlow()

    private val _sendMessageResult = MutableStateFlow<Result<String>?>(null)
    val sendMessageResult: StateFlow<Result<String>?> = _sendMessageResult.asStateFlow()

    private val _userChatsState = MutableStateFlow<Result<List<ChatUserInfo>>>(Result.Loading())
    val userChatsState: StateFlow<Result<List<ChatUserInfo>>> = _userChatsState.asStateFlow()

    private val _chatIdState = MutableStateFlow<Result<String>>(Result.Loading())
    val chatIdState: StateFlow<Result<String>> = _chatIdState.asStateFlow()

    private val _readStatusResult = MutableStateFlow<Result<SimpleResponse>?>(null)
    val readStatusResult: StateFlow<Result<SimpleResponse>?> = _readStatusResult.asStateFlow()

    fun loadMessages(chatId: Int, limit: Int = 100, offset: Long = 0) {
        viewModelScope.launch {
            _messagesState.value = Result.Loading()
            val result = userRepo.getMessages(chatId, limit, offset)
            _messagesState.value = result
        }
    }

    fun sendMessage(chatId: Int, text: String) {
        viewModelScope.launch {
            _sendMessageResult.value = Result.Loading()
            val result = userRepo.sendMessage(chatId, text)
            _sendMessageResult.value = result
        }
    }

    fun startChat(otherUserId: Int) {
        viewModelScope.launch {
            _chatIdState.value = Result.Loading()
            val result = userRepo.startChat(otherUserId)
            _chatIdState.value = result
        }
    }

    fun getUserChats() {
        viewModelScope.launch {
            _userChatsState.value = Result.Loading()
            val result = userRepo.getUserChatsWithInfo()
            _userChatsState.value = result
        }
    }

    fun updateReadStatus(chatId: Int, isRead: Boolean) {
        viewModelScope.launch {
            _readStatusResult.value = Result.Loading()
            val result = userRepo.updateReadStatus(chatId, isRead)
            _readStatusResult.value = result
        }
    }

    fun clearSendState() {
        _sendMessageResult.value = null
    }

    fun clearReadStatusState() {
        _readStatusResult.value = null
    }
}
