package com.vt.shoppet.util

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class PreferenceUtils @Inject constructor(private val preferences: SharedPreferences) {

    fun getTheme() = preferences.getInt("theme", 0)

    fun setTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        preferences.edit().putInt("theme", mode).apply()
    }

}