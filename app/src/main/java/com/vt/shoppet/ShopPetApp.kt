package com.vt.shoppet

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShopPetApp: Application() {

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        val mode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) preferences.getInt("theme", -1)
            else preferences.getInt("theme", 3)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

}