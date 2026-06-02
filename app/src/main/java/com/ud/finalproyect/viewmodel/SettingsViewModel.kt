package com.ud.finalproyect.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    // Controlamos el estado del switch local
    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled = _darkModeEnabled.asStateFlow()

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    // IDIOMA GLOBAL
    fun changeLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    // MODO OSCURO GLOBAL (CORREGIDO)
    fun toggleDarkMode(enabled: Boolean) {
        _darkModeEnabled.value = enabled
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}