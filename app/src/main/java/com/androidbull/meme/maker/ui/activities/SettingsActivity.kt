package com.androidbull.meme.maker.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import com.androidbull.meme.maker.R
import com.androidbull.meme.maker.helper.*
import com.androidbull.meme.maker.helper.SettingsManager.getFullScreenModePreference
import com.androidbull.meme.maker.helper.ThemeHelper.setAppropriateTheme
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : BaseActivity() {

    private lateinit var tbSettings: MaterialToolbar
    private lateinit var llRemoveAds: LinearLayout
    private lateinit var llCustomFonts: LinearLayout
    private lateinit var llDownloadAllMemes: LinearLayout
    private lateinit var llFullScreenMode: LinearLayout
    private lateinit var llDarkMode: LinearLayout
    private lateinit var cbFullScreen: CheckBox
    private lateinit var cbDarkMode: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initUi()
        initToolbar()
        initActions()
        initSettings()

        PremiumMemberObservable.isPremiumUser.observe(this, { isPremium ->
            if (!isPremium)
                llRemoveAds.visibility = View.VISIBLE
        })
    }


    private fun initUi() {
        llRemoveAds = findViewById(R.id.llRemoveAds)
        llCustomFonts = findViewById(R.id.llCustomFonts)
        llDownloadAllMemes = findViewById(R.id.llDownloadAllMemes)
        llFullScreenMode = findViewById(R.id.llFullScreenMode)
        llDarkMode = findViewById(R.id.llDarkMode)
        cbFullScreen = findViewById(R.id.cbFullScreen)
        cbDarkMode = findViewById(R.id.cbDarkMode)
        tbSettings = findViewById(R.id.tbSettings)

    }

    private fun initActions() {
        llRemoveAds.setOnClickListener {
            startActivity(Intent(this, PurchaseActivity::class.java))
        }

        llCustomFonts.setOnClickListener {
            startActivity(Intent(this, CustomFontActivity::class.java))
        }

        llDownloadAllMemes.setOnClickListener {
            startActivity(Intent(this, DownloadAllMemeActivity::class.java))
        }

        llFullScreenMode.setOnClickListener {
            onFullScreenModeClicked()
        }

        llDarkMode.setOnClickListener {
            onDarkModeClicked()
        }

        cbFullScreen.setOnCheckedChangeListener { _, isChecked ->
            toggleFullScreenMode(isChecked)
        }

        cbDarkMode.setOnClickListener {
            toggleDarkMode(cbDarkMode.isChecked)
        }
    }


    private fun initToolbar() {
        setSupportActionBar(tbSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    private fun initSettings() {
        cbFullScreen.isChecked = getFullScreenModePreference()

        when (SettingsManager.getUiThemeModePreference()) {
            ThemeHelper.UiMode.DARK.ordinal -> {
                cbDarkMode.isChecked = true
            }
            ThemeHelper.UiMode.LIGHT.ordinal -> {
                cbDarkMode.isChecked = false
            }
            // when following system default mode, update preferences accordingly
            // for runtime theme change
            ThemeHelper.UiMode.DEFAULT.ordinal -> {
                when (AppContext.getInstance().context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        cbDarkMode.isChecked = false
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        cbDarkMode.isChecked = true
                    }
                }
            }
        }
    }


    private fun onFullScreenModeClicked() {
        cbFullScreen.isChecked = !cbFullScreen.isChecked
    }

    private fun onDarkModeClicked() {
        cbDarkMode.performClick()
    }

    private fun toggleFullScreenMode(isChecked: Boolean) {
        SettingsManager.saveFullScreenModePreference(isChecked)
        setFullScreen(isChecked, this)
    }

    private fun toggleDarkMode(isEnableDarkMode: Boolean) {
        if (isEnableDarkMode) {
            SettingsManager.saveUiThemeModePreference(ThemeHelper.UiMode.DARK.ordinal)
        } else {
            SettingsManager.saveUiThemeModePreference(ThemeHelper.UiMode.LIGHT.ordinal)
        }

        setAppropriateTheme()
    }

}