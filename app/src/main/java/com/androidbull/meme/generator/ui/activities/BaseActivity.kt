package com.androidbull.meme.generator.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidbull.meme.generator.helper.PREF_FULL_SCREEN
import com.androidbull.meme.generator.helper.PreferenceManager
import com.androidbull.meme.generator.helper.setFullScreen

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSetFullScreenMode()

    }

    override fun onResume() {
        getSetFullScreenMode()  // if activity is Already started
        super.onResume()
    }

    private fun getSetFullScreenMode() {
        setFullScreen(PreferenceManager.getInstance().getBoolean(PREF_FULL_SCREEN), this)
    }

}