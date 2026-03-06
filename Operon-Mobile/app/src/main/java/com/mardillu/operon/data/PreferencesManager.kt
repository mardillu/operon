package com.mardillu.operon.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ExecutionMode {
    ALWAYS_EXECUTE,
    ASK_SOME_RECOMMENDED,
    ALWAYS_ASK
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val EXECUTION_MODE_KEY = stringPreferencesKey("execution_mode")
    }

    val executionModeFlow: Flow<ExecutionMode> = context.dataStore.data
        .map { preferences ->
            val modeString = preferences[EXECUTION_MODE_KEY] ?: ExecutionMode.ASK_SOME_RECOMMENDED.name
            try {
                ExecutionMode.valueOf(modeString)
            } catch (e: IllegalArgumentException) {
                ExecutionMode.ASK_SOME_RECOMMENDED
            }
        }

    suspend fun saveExecutionMode(mode: ExecutionMode) {
        context.dataStore.edit { preferences ->
            preferences[EXECUTION_MODE_KEY] = mode.name
        }
    }
}
