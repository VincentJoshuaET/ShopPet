package com.vt.shoppet.ui

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThemeDialog : DialogFragment() {

    @Inject
    lateinit var preferences: SharedPreferences

    private fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        preferences.edit().putInt("theme", mode).apply()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var item = preferences.getInt("theme", 0)
        if (item == -1 || item == 3) item = 0
        val items =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) resources.getStringArray(R.array.theme_new)
            else resources.getStringArray(R.array.theme_old)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.menu_item_theme)
            .setSingleChoiceItems(items, item) { dialog, which ->
                when (which) {
                    0 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        } else setThemeMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    }
                    1 -> setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                dialog.dismiss()
            }
            .create()
    }

}