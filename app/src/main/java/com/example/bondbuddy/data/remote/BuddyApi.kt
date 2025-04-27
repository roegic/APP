package com.example.bondbuddy.data.remote

import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.data.remote.models.LoginRequest
import com.example.bondbuddy.data.remote.models.RegisterRequest
import com.example.bondbuddy.data.remote.models.SimpleResponse
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.utils.Constants.API_VERSION
import com.example.bondbuddy.data.remote.models.Language
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BuddyApi {


    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/register")
    suspend fun registerUser(
        @Body registerRequest: RegisterRequest
    ): SimpleResponse

    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): SimpleResponse

    // USER INFO

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/getUserInfo")
    suspend fun getUserInfo(
        @Header("Authorization") token:String,
    ): UserInfo

    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/update/userInfo")
    suspend fun updateUserInfo(
        @Header("Authorization") token:String,
        @Body userInfo: UserInfo
    ): SimpleResponse

    // USER INTERESTS
    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/getUserInterests")
    suspend fun getUserInterests(
        @Header("Authorization") token:String,
    ): List<Interest>

    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/update/userInterests")
    suspend fun updateUserInterests(
        @Header("Authorization") token:String,
        @Body userInterests: List<Interest>
    ): SimpleResponse

    // USER SOCIALS

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/socials")
    suspend fun getUserSocials(
        @Header("Authorization") token:String,
    ): List<SocialMedia>

    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/update/socials")
    suspend fun updateUserSocials(
        @Header("Authorization") token:String,
        @Body userInterests: List<SocialMedia>
    ): SimpleResponse

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/discover/getDefaultRecommendation")
    suspend fun getDefaultUserRecommendations(
        @Header("Authorization") token:String,
    ): List<UserInfo>

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/discover/getFilteredRecommendation")
    suspend fun getFilteredUserRecommendations(
        @Header("Authorization") token: String,
        @Query("interests") selectedInterests: List<String>?,
        @Query("minAge") minAge: String?,
        @Query("maxAge") maxAge: String?,
        @Query("city") city: String?,
        @Query("country") country: String?,
        @Query("languages") languages: List<String>?
    ): List<UserInfo>


    // PROFILE CARD
    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/user/{id}/interests")
    suspend fun getUserInterestsById(
        @Header("Authorization") token:String,
        @Path("id") userId: String
    ): List<Interest>

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/user/{id}/socials")
    suspend fun getUserSocialsById(
        @Header("Authorization") token:String,
        @Path("id") userId: String
    ): List<SocialMedia>

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/user/{id}/info")
    suspend fun getUserInfoById(
        @Header("Authorization") token:String,
        @Path("id") userId: String
    ): UserInfo

    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/follow/{followId}")
    suspend fun followUser(
        @Header("Authorization") token: String,
        @Path("followId") followId: Int
    ): SimpleResponse


    @Headers("Content-Type: application/json")
    @DELETE("$API_VERSION/users/unfollow/{unfollowId}")
    suspend fun unfollowUser(
        @Header("Authorization") token: String,
        @Path("unfollowId") unfollowId: Int
    ): SimpleResponse

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/user/followers")
    suspend fun getUserFollowerList(
        @Header("Authorization") token:String
    ): List<UserInfo>

    // LANGUAGES

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/supportedLanguagesList")
    suspend fun getSupportedLanguagesList(
        @Header("Authorization") token:String
    ): List<Language>

    @Headers("Content-Type: application/json")
    @POST("$API_VERSION/users/update/languages")
    suspend fun updateUserLanguages(
        @Header("Authorization") token:String,
        @Body userInterests: List<String>
    ): SimpleResponse

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/languages")
    suspend fun getUserLanguages(
        @Header("Authorization") token:String,
    ): List<Language>

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/user/{id}/languages")
    suspend fun getUserLanguagesById(
        @Header("Authorization") token:String,
        @Path("id") userId: String
    ): List<Language>

    @Headers("Content-Type: application/json")
    @GET("$API_VERSION/users/get/interests/tags")
    suspend fun getAllInterestsTags(
        @Header("Authorization") token:String
    ): List<String>



}