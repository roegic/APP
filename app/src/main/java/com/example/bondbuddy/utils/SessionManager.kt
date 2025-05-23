package com.example.bondbuddy.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bondbuddy.utils.Constants.EMAIL_KEY
import com.example.bondbuddy.utils.Constants.JWT_TOKEN_KEY
import com.example.bondbuddy.utils.Constants.NAME_KEY
import kotlinx.coroutines.flow.first

class SessionManager(val context: Context) {

    private val Context.dataStore: DataStore<Preferences>  by preferencesDataStore("session manager")

    suspend fun updateSession(token:String,email:String) {
        val jwtTokenKey = stringPreferencesKey(JWT_TOKEN_KEY)
        val emailKey = stringPreferencesKey(EMAIL_KEY)
        val nameKey = stringPreferencesKey(NAME_KEY)

        context.dataStore.edit { preferences ->
            preferences[jwtTokenKey] = token
            preferences[emailKey] = email

        }
    }

    suspend fun getJwtToken(): String? {
        val jwtTokenKey = stringPreferencesKey(JWT_TOKEN_KEY)
        val preferences = context.dataStore.data.first()
        return preferences[jwtTokenKey]
    }

    suspend fun getCurrentUserName():String? {
        val nameKey = stringPreferencesKey(NAME_KEY)
        val preferences = context.dataStore.data.first()
        return preferences[nameKey]
    }

    suspend fun getCurrentUserEmail():String? {
        val email = stringPreferencesKey(EMAIL_KEY)
        val preferences = context.dataStore.data.first()
        return preferences[email]
    }

    suspend fun logout() {
        context.dataStore.edit {
            it.clear()
        }
    }
}