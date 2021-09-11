package com.skyyo.igdbbrowser.application.persistance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.skyyo.igdbbrowser.features.sampleContainer.THEME_AUTO
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton


val Context.getDataStore: DataStore<Preferences> by preferencesDataStore(name = "templateDataStore")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.getDataStore

    suspend fun setAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = "Bearer $accessToken"
        }
    }

    suspend fun getAccessToken(): String? =
        dataStore.data.first()[PreferencesKeys.ACCESS_TOKEN]

    suspend fun setAppTheme(appTheme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = appTheme
        }
    }

    suspend fun getAppTheme(): String = dataStore.data.first()[PreferencesKeys.APP_THEME]?: THEME_AUTO

    suspend fun clearData() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        val APP_THEME = stringPreferencesKey("appTheme")
    }
}
