package com.example.bondbuddy.repo

import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.data.remote.models.Language
import com.example.bondbuddy.data.remote.models.LoginRequest
import com.example.bondbuddy.data.remote.models.RegisterRequest
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.data.remote.models.User
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.utils.Result
interface UserRepo {
    suspend fun createUser(registerRequest: RegisterRequest):Result<String>
    suspend fun login(loginRequest: LoginRequest):Result<String>
    suspend fun getUser():Result<User>
    suspend fun logout():Result<String>

    suspend fun getUserInfo(): Result<UserInfo>
    suspend fun updateUserInfo(userInfo: UserInfo): Result<SimpleResponse>

    suspend fun getUserInterests(): Result<List<Interest>>
    suspend fun updateUserInterests(userInterests: List<Interest>): Result<SimpleResponse>

    suspend fun updateUserSocials(userSocials: List<SocialMedia>): Result<SimpleResponse>
    suspend fun getUserSocials(): Result<List<SocialMedia>>

    suspend fun getUserDefaultRecommendations(): Result<List<UserInfo>>
    suspend fun getFilteredUserRecommendations(selectedInterests: List<String>?, minAge: String?, maxAge: String?, city: String?, country: String?, languages: List<String>?): Result<List<UserInfo>>

    suspend fun getUserInterestsById(userId: String): Result<List<Interest>>
    suspend fun getUserInfoById(userId: String): Result<UserInfo>
    suspend fun getUserSocialsById(userId: String): Result<List<SocialMedia>>

    suspend fun followUser(followId: Int): Result<SimpleResponse>
    suspend fun unfollowUser(unfollowId: Int): Result<SimpleResponse>

    suspend fun getUserFollowerList(): Result<List<UserInfo>>

    suspend fun getSupportedLanguagesList(): Result<List<Language>>
    suspend fun updateUserLanguages(userLanguages: List<String>): Result<SimpleResponse>

    suspend fun getUserLanguages(): Result<List<Language>>
    suspend fun getUserLanguagesById(userId: String): Result<List<Language>>

    suspend fun getAllInterestsTags(): Result<List<String>>

}