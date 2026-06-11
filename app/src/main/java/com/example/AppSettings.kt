package com.example

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object AppSettings {
    val PIN_KEY = stringPreferencesKey("app_pin")

    fun getPin(context: Context) = context.dataStore.data.map { prefs ->
        prefs[PIN_KEY]
    }

    suspend fun setPin(context: Context, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PIN_KEY] = pin
        }
    }
}
