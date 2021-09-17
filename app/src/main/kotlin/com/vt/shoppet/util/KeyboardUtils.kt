package com.vt.shoppet.util

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class KeyboardUtils @Inject constructor(private val activity: Activity) {

    fun hide() {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)?.hide(WindowInsetsCompat.Type.ime())
    }

}