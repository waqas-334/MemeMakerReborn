package com.androidbull.meme.generator.helper

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager {

    companion object {
        private val sharePref = PreferenceManager()
        private var mSharedPreferences: SharedPreferences? = null

        fun getInstance(): PreferenceManager {
            if (mSharedPreferences == null) {
                mSharedPreferences = AppContext.getInstance().context.getSharedPreferences(
                    PREF_FILE_KEY,
                    Context.MODE_PRIVATE
                )
            }
            return sharePref
        }
    }

    fun setString(key: String, value: String) {
        mSharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String) = mSharedPreferences!!.getString(key, "")


    fun setInt(key: String, value: Int) {
        mSharedPreferences?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(key: String) = mSharedPreferences!!.getInt(key, 0)

    fun getInt(key: String, defaultValue: Int) = mSharedPreferences!!.getInt(key, defaultValue)


    fun setLong(key: String, value: Long) {
        mSharedPreferences?.edit()?.putLong(key, value)?.apply()
    }

    fun getLong(key: String) = mSharedPreferences!!.getLong(key, 0)


    fun setBoolean(key: String, value: Boolean) {
        mSharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String) = mSharedPreferences!!.getBoolean(key, false)

    fun getBoolean(key: String, defaultValue: Boolean) =
        mSharedPreferences!!.getBoolean(key, defaultValue)

    fun clearPreferences() {
        mSharedPreferences?.edit()?.clear()?.apply()
    }

    fun getStringSet(key: String): Set<String> {
        return mSharedPreferences!!.getStringSet(key, setOf<String>())!!
    }

    fun putStringSet(key: String, stringSet: Set<String>) {
        mSharedPreferences?.edit()?.putStringSet(key, stringSet)?.apply()
    }
}