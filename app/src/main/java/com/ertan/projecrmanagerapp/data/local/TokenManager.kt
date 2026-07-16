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
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val SENIORITY_KEY = stringPreferencesKey("user_seniority")
    }

    suspend fun saveAuthData(token: String, role: String, name: String, userId: Int, seniority: String?) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[ROLE_KEY] = role
            prefs[NAME_KEY] = name
            prefs[USER_ID_KEY] = userId.toString()
            if(seniority != null){
                prefs[SENIORITY_KEY] = seniority
            } else{
                prefs.remove(SENIORITY_KEY)
            }
        }
    }

    fun getToken(): Flow<String?> =
        context.dataStore.data.map { it[TOKEN_KEY] }

    fun getRole(): Flow<String?> =
        context.dataStore.data.map { it[ROLE_KEY] }

    fun getName(): Flow<String?> =
        context.dataStore.data.map { it[NAME_KEY]}
    fun getSeniority(): Flow<String?> =
        context.dataStore.data.map { it[SENIORITY_KEY]}

    fun getUserId(): Flow<Int?> =
        context.dataStore.data.map { it[USER_ID_KEY]?.toIntOrNull() }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}