package com.example.bondbuddy.repo

import com.example.bondbuddy.data.remote.BuddyApi
import com.example.bondbuddy.data.remote.UpdateReadStatusRequest
import com.example.bondbuddy.data.remote.models.ChatUserInfo
import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.data.remote.models.Language
import com.example.bondbuddy.data.remote.models.LoginRequest
import com.example.bondbuddy.data.remote.models.Message
import com.example.bondbuddy.data.remote.models.RegisterRequest
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.data.remote.models.User
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.utils.SessionManager
import com.example.bondbuddy.utils.isNetworkConnected
import javax.inject.Inject
import com.example.bondbuddy.utils.Result

class UserRepoImpl @Inject constructor(val buddyApi: BuddyApi, val sessionManager: SessionManager): UserRepo{
    override suspend fun createUser(registerRequest: RegisterRequest): Result<String>{
        return try {
            if(!isNetworkConnected(sessionManager.context)) {
                Result.Error<String>("No internet connection")
            }
            val result = buddyApi.registerUser(registerRequest)
            if (result.success) {
                sessionManager.updateSession(result.message, registerRequest.email) // message is jwt token
                Result.Success("User created successfully")
            } else {
                Result.Error<String>(result.message)
            }
        } catch (e:Exception) {
            e.printStackTrace()
            Result.Error<String>(e.message ?: "Some error occurred")
        }
    }

    override suspend fun login(loginRequest: LoginRequest): Result<String> {
        return try {
            if(!isNetworkConnected(sessionManager.context)){
                Result.Error<String>("No Internet Connection!")
            }

            val result = buddyApi.loginUser(loginRequest)
            if(result.success){
                sessionManager.updateSession(result.message,loginRequest.email)
                Result.Success("Logged In Successfully!")
            } else {
                Result.Error<String>(result.message)
            }
        }catch (e:Exception) {
            e.printStackTrace()
            Result.Error<String>(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUser(): Result<User> {
        return try {
            val name = sessionManager.getCurrentUserName()
            val email = sessionManager.getCurrentUserEmail()
            if(name == null || email == null){
                return Result.Error<User>("User not Logged In!")
            }
            return Result.Success(User(-1,email, name,""))
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }

    }

    override suspend fun getUserInfo(): Result<UserInfo> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<UserInfo>("User not Logged In!");
            val result = buddyApi.getUserInfo("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }

    }

    override suspend fun updateUserInfo(userInfo: UserInfo): Result<SimpleResponse> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<SimpleResponse>("User not Logged In!");
            val result = buddyApi.updateUserInfo("Bearer $token", userInfo)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }

    }

    override suspend fun getUserInterests(): Result<List<Interest>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<Interest>>("User not Logged In!");
            val result = buddyApi.getUserInterests("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }

    }

    override suspend fun updateUserInterests(userInterests: List<Interest>): Result<SimpleResponse> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<SimpleResponse>("User not Logged In!");
            val result = buddyApi.updateUserInterests("Bearer $token", userInterests)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    // SOCIALS

    override suspend fun updateUserSocials(userSocials: List<SocialMedia>): Result<SimpleResponse> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<SimpleResponse>("User not Logged In!");
            val result = buddyApi.updateUserSocials("Bearer $token", userSocials)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserSocials(): Result<List<SocialMedia>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<SocialMedia>>("User not Logged In!");
            val result = buddyApi.getUserSocials("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }

    }

    override suspend fun getUserDefaultRecommendations(): Result<List<UserInfo>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<UserInfo>>("User not Logged In!");
            val result = buddyApi.getDefaultUserRecommendations("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getFilteredUserRecommendations(selectedInterests: List<String>?, minAge: String?, maxAge: String?, city: String?, country: String?, languages: List<String>?): Result<List<UserInfo>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<UserInfo>>("User not Logged In!");
            val result = buddyApi.getFilteredUserRecommendations("Bearer $token", selectedInterests, minAge, maxAge, city, country, languages)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserInterestsById(userId: String): Result<List<Interest>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<Interest>>("User not Logged In!")
            val result = buddyApi.getUserInterestsById("Bearer $token",userId)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserInfoById(userId: String): Result<UserInfo> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<UserInfo>("User not Logged In!")
            val result = buddyApi.getUserInfoById("Bearer $token",userId)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserSocialsById(userId: String): Result<List<SocialMedia>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<SocialMedia>>("User not Logged In!")
            val result = buddyApi.getUserSocialsById("Bearer $token",userId)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }


    override suspend fun logout(): Result<String> {
        return try {
            sessionManager.logout()
            Result.Success("Logged Out Successfully!")
        } catch (e:Exception){
            e.printStackTrace()
            Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }


    // FOLLOW
    override suspend fun followUser(followId: Int): Result<SimpleResponse> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<SimpleResponse>("User not Logged In!")
            val result = buddyApi.followUser("Bearer $token", followId)
            return Result.Success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun unfollowUser(unfollowId: Int): Result<SimpleResponse> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<SimpleResponse>("User not Logged In!")
            val result = buddyApi.unfollowUser("Bearer $token", unfollowId)
            return Result.Success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserFollowerList(): Result<List<UserInfo>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<UserInfo>>("User not Logged In!")
            val result = buddyApi.getUserFollowerList("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getSupportedLanguagesList(): Result<List<Language>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<Language>>("User not Logged In!")
            val result = buddyApi.getSupportedLanguagesList("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getAllInterestsTags(): Result<List<String>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<String>>("User not Logged In!")
            val result = buddyApi.getAllInterestsTags("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun updateUserLanguages(userLanguages: List<String>): Result<SimpleResponse> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<SimpleResponse>("User not Logged In!");
            val result = buddyApi.updateUserLanguages("Bearer $token", userLanguages)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserLanguages(): Result<List<Language>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<Language>>("User not Logged In!")
            val result = buddyApi.getUserLanguages("Bearer $token")
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    override suspend fun getUserLanguagesById(userId: String): Result<List<Language>> {
        try {
            val token = sessionManager.getJwtToken() ?: return Result.Error<List<Language>>("User not Logged In!")
            val result = buddyApi.getUserLanguagesById("Bearer $token", userId)
            return Result.Success(result)
        } catch (e:Exception){
            e.printStackTrace()
            return Result.Error(e.message ?: "Some Problem Occurred!")
        }
    }

    // chat

    override suspend fun startChat(otherUserId: Int): Result<String> {
        return try {
            val token = sessionManager.getJwtToken() ?: return Result.Error("User not logged in!")
            val response = buddyApi.startChat("Bearer $token", otherUserId)
            if (response.success) {
                Result.Success(response.message)
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Failed to start chat")
        }
    }

    override suspend fun getMessages(chatId: Int, limit: Int, offset: Long): Result<List<Message>> {
        return try {
            val token = sessionManager.getJwtToken() ?: return Result.Error("User not logged in!")
            val messages = buddyApi.getMessages("Bearer $token", chatId, limit, offset)
            Result.Success(messages)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Failed to get messages")
        }
    }

    override suspend fun sendMessage(chatId: Int, text: String): Result<String> {
        return try {
            val token = sessionManager.getJwtToken() ?: return Result.Error("User not logged in!")
            val request = Message(chatId, -1, text, -1, false)
            val response = buddyApi.sendMessage("Bearer $token", chatId, request)
            if (response.success) {
                Result.Success(response.message)
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Failed to send message")
        }
    }

    override suspend fun getUserChatsWithInfo(): Result<List<ChatUserInfo>> {
        return try {
            val token = sessionManager.getJwtToken() ?: return Result.Error("User not logged in!")
            val chats = buddyApi.getUserChats("Bearer $token")
            Result.Success(chats)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Failed to get user chats")
        }
    }

    override suspend fun updateReadStatus(chatId: Int, isRead: Boolean): Result<SimpleResponse> {
        return try {
            val token = sessionManager.getJwtToken() ?: return Result.Error("User not logged in!")
            val response = buddyApi.updateReadStatus("Bearer $token", chatId, UpdateReadStatusRequest(isRead))
            Result.Success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Failed to update read status")
        }
    }


}