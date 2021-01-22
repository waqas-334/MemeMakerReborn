package com.androidbull.meme.maker.helper

import androidx.appcompat.app.AppCompatDelegate


object ThemeHelper {

    enum class UiMode {
        DEFAULT,
        LIGHT,
        DARK,
        BATTERY_SAVER_MODE
    }

    fun setAppropriateTheme() {

        val uiMode: UiMode
        when (SettingsManager.getUiThemeModePreference()) {
            UiMode.DEFAULT.ordinal -> {
                uiMode = UiMode.DEFAULT
            }
            UiMode.LIGHT.ordinal -> {
                uiMode = UiMode.LIGHT
            }
            UiMode.DARK.ordinal -> {
                uiMode = UiMode.DARK
            }
            UiMode.BATTERY_SAVER_MODE.ordinal -> {
                uiMode = UiMode.BATTERY_SAVER_MODE
            }
            else -> {
                uiMode = UiMode.DEFAULT
            }
        }
        applyTheme(uiMode)
    }

    private fun applyTheme(theme: UiMode) {
        when (theme) {
            UiMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            UiMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            UiMode.BATTERY_SAVER_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            UiMode.DEFAULT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}