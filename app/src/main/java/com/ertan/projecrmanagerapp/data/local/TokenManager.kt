package com.ertan.projecrmanagerapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val ROLE_KEY = stringPreferencesKey("user_role")
        val NAME_KEY = stringPreferencesKey("user_name")
    }

    suspend fun saveAuthData(token: String, role: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[ROLE_KEY] = role
            prefs[NAME_KEY] = name
        }
    }

    fun getToken(): Flow<String?> =
        context.dataStore.data.map { it[TOKEN_KEY] }

    fun getRole(): Flow<String?> =
        context.dataStore.data.map { it[ROLE_KEY] }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}