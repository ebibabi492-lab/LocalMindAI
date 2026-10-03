package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("localmind_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            language = prefs.getString("language", "system") ?: "system",
            theme = prefs.getString("theme", "system") ?: "system",
            maxResponseTokens = prefs.getInt("max_tokens", 1024),
            temperature = prefs.getFloat("temperature", 0.7f),
            activeModelId = prefs.getString("active_model_id", null),
            preferredBackend = prefs.getString("preferred_backend", "cpu") ?: "cpu"
        )
    }

    fun updateLanguage(language: String) {
        prefs.edit().putString("language", language).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun updateTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateTemperature(temp: Float) {
        prefs.edit().putFloat("temperature", temp).apply()
        _settings.value = _settings.value.copy(temperature = temp)
    }

    fun updateMaxTokens(tokens: Int) {
        prefs.edit().putInt("max_tokens", tokens).apply()
        _settings.value = _settings.value.copy(maxResponseTokens = tokens)
    }

    fun updateActiveModelId(modelId: String?) {
        prefs.edit().putString("active_model_id", modelId).apply()
        _settings.value = _settings.value.copy(activeModelId = modelId)
    }

    fun updatePreferredBackend(backend: String) {
        prefs.edit().putString("preferred_backend", backend).apply()
        _settings.value = _settings.value.copy(preferredBackend = backend)
    }
}
