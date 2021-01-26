package com.androidbull.meme.maker.helper

import android.text.TextUtils
import com.androidbull.meme.maker.model.CaptionSetting
import com.androidbull.meme.maker.model.getDefaultCaptionSettings
import com.google.gson.Gson

object SettingsManager {

    private val preferenceManager =
        PreferenceManager.getInstance()

    fun saveCaptionSettings(captionSetting: CaptionSetting) {
        val captionSettingJsonStr = Gson().toJson(captionSetting, CaptionSetting::class.java)
        preferenceManager.setString(
            PREF_CAPTION_SETTINGS,
            captionSettingJsonStr
        )
    }

    fun getCaptionSettings(): CaptionSetting {
        val contactJsonStr =
            preferenceManager.getString(PREF_CAPTION_SETTINGS)
        return if (!TextUtils.isEmpty(contactJsonStr)) {
            Gson().fromJson(contactJsonStr, CaptionSetting::class.java)
        } else {
            getDefaultCaptionSettings()
        }
    }

    fun saveCurrentLayoutManager(layoutManagerOwner: String, layoutManagerType: LayoutManagerType) {
        preferenceManager.setInt(layoutManagerOwner, layoutManagerType.ordinal)
    }

    fun getCurrentLayoutManager(layoutManagerOwner: String): LayoutManagerType {
        val layoutManagerType = preferenceManager.getInt(layoutManagerOwner)

        if (layoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER.ordinal) {
            return LayoutManagerType.GRID_LAYOUT_MANAGER
        } else if (layoutManagerType == LayoutManagerType.LINEAR_LAYOUT_MANAGER.ordinal) {
            return LayoutManagerType.LINEAR_LAYOUT_MANAGER
        } else {
            return LayoutManagerType.GRID_LAYOUT_MANAGER
        }
    }

    fun getRecentlyUsedColors(): MutableList<String> {
        var recentlyUsedColorsList = listOf<String>()
        val recentlyUsedColorsStr = preferenceManager.getString(PREF_RECENTLY_USED_COLORS)
        recentlyUsedColorsStr?.let {
            if (it.isNotEmpty()) {
                recentlyUsedColorsList = it.split(";")
            }
        }

        return recentlyUsedColorsList.toMutableList()
    }

    fun saveRecentlyUsedColors(recentlyUsedColors: List<String>) {
        val recentlyUsedColorsStr = TextUtils.join(";", recentlyUsedColors)
        preferenceManager.setString(PREF_RECENTLY_USED_COLORS, recentlyUsedColorsStr)
    }

    fun saveDefaultRecentlyUsedColors() {
        saveRecentlyUsedColors(
            listOf(
                "#FF000000",
                "#FF000000",
                "#FFFFFFFF",
                "#FFFFFFFF"
            )
        )  //default colors
    }

    fun saveFullScreenModePreference(isFullScreenMode: Boolean) {
        preferenceManager.setBoolean(PREF_FULL_SCREEN, isFullScreenMode)
    }

    fun getFullScreenModePreference() =
        preferenceManager.getBoolean(PREF_FULL_SCREEN)

    fun saveUiThemeModePreference(uiThemeMode: Int) {
        preferenceManager.setInt(PREF_UI_THEME_MODE, uiThemeMode)
    }

    fun getUiThemeModePreference() = preferenceManager.getInt(PREF_UI_THEME_MODE)

    fun saveNewMemesAvailable(areNewMemesAvailable: Boolean) {
        preferenceManager.setBoolean(PREF_ARE_NEW_MEMES_AVAILABLE, areNewMemesAvailable)
    }
    fun getNewMemesAvailable() = preferenceManager.getBoolean(PREF_ARE_NEW_MEMES_AVAILABLE)

    fun getAppLaunchCount() = preferenceManager.getInt(PREF_APP_LAUNCH_COUNT)
    fun saveAppLaunchCount(appLaunchCount: Int) {
        preferenceManager.setInt(PREF_APP_LAUNCH_COUNT, appLaunchCount)
    }

    fun getIsAppRated() = preferenceManager.getBoolean(PREF_IS_APP_RATED)
    fun saveIsAppRated(isAppRated: Boolean) {
        preferenceManager.setBoolean(PREF_IS_APP_RATED, isAppRated)
    }

    fun getIsAllMemesDownloaded() = preferenceManager.getBoolean(PREF_ARE_ALL_MEME_DOWNLOADED)
    fun saveIsAllMemesDownloaded(isAllMemesDownloaded: Boolean) {
        preferenceManager.setBoolean(PREF_ARE_ALL_MEME_DOWNLOADED, isAllMemesDownloaded)
    }
}

