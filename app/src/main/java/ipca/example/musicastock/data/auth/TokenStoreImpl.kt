package ipca.example.musicastock.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenStore {

    private val KEY_TOKEN = stringPreferencesKey("auth_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id") // Nova chave

    override val tokenFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }

    override val userIdFlow: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_USER_ID] }

    override suspend fun getToken(): String? = tokenFlow.first()

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    override suspend fun getUserId(): String? = userIdFlow.first()

    override suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs -> prefs[KEY_USER_ID] = userId }
    }

    override suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
        }
    }

}