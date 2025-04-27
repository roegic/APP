package com.example.bondbuddy.di

import android.content.Context
import com.example.bondbuddy.data.remote.BuddyApi
import com.example.bondbuddy.repo.UserRepo
import com.example.bondbuddy.repo.UserRepoImpl
import com.example.bondbuddy.utils.Constants.BASE_URL
import com.example.bondbuddy.utils.SessionManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideGson() = Gson()

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) = SessionManager(context)

    @Singleton
    @Provides
    fun provideBuddyApi(): BuddyApi {
        val httpLoginInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder().addInterceptor(httpLoginInterceptor).build()

        return Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(BuddyApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserRepo(buddyApi: BuddyApi,sessionManager: SessionManager): UserRepo {
        return UserRepoImpl(buddyApi,sessionManager)
    }
}