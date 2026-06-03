package com.ud.finalproyect.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ud.finalproyect.model.repository.NotificationPreferenceRepository
import com.ud.finalproyect.model.data.NotificationPreference

class SettingsViewModel : ViewModel() {

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled = _darkModeEnabled.asStateFlow()

    private val _doNotDisturbEnabled = MutableStateFlow(false)
    val doNotDisturbEnabled = _doNotDisturbEnabled.asStateFlow()

    private val _doNotDisturbStart = MutableStateFlow("21:00")
    val doNotDisturbStart = _doNotDisturbStart.asStateFlow()

    private val _doNotDisturbEnd = MutableStateFlow("08:00")
    val doNotDisturbEnd = _doNotDisturbEnd.asStateFlow()

    private val _snoozeIntervalMinutes = MutableStateFlow(15)
    val snoozeIntervalMinutes = _snoozeIntervalMinutes.asStateFlow()

    private val notificationPreferenceRepository = NotificationPreferenceRepository()

    init {
        loadNotificationPreferences()
    }

    private fun loadNotificationPreferences() {
        viewModelScope.launch {
            notificationPreferenceRepository.getPreferences().collect { preference ->
                _notificationsEnabled.value = preference.enableNotifications
                _doNotDisturbEnabled.value = preference.enableDoNotDisturb
                _doNotDisturbStart.value = preference.doNotDisturbStart
                _doNotDisturbEnd.value = preference.doNotDisturbEnd
                _snoozeIntervalMinutes.value = preference.snoozeIntervalMinutes
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch {
            notificationPreferenceRepository.getPreferences().collect { preference ->
                val updated = preference.copy(enableNotifications = enabled)
                notificationPreferenceRepository.updatePreferences(updated)
            }
        }
    }

    fun toggleDoNotDisturb(enabled: Boolean) {
        _doNotDisturbEnabled.value = enabled
        notificationPreferenceRepository.updateDoNotDisturb(
            enabled,
            _doNotDisturbStart.value,
            _doNotDisturbEnd.value
        )
    }

    fun updateDoNotDisturbStart(time: String) {
        _doNotDisturbStart.value = time
        notificationPreferenceRepository.updateDoNotDisturb(
            _doNotDisturbEnabled.value,
            time,
            _doNotDisturbEnd.value
        )
    }

    fun updateDoNotDisturbEnd(time: String) {
        _doNotDisturbEnd.value = time
        notificationPreferenceRepository.updateDoNotDisturb(
            _doNotDisturbEnabled.value,
            _doNotDisturbStart.value,
            time
        )
    }

    fun updateSnoozeInterval(minutes: Int) {
        _snoozeIntervalMinutes.value = minutes
        notificationPreferenceRepository.updateSnoozeInterval(minutes)
    }

    // IDIOMA GLOBAL
    fun changeLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    // MODO OSCURO GLOBAL
    fun toggleDarkMode(enabled: Boolean) {
        _darkModeEnabled.value = enabled
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}