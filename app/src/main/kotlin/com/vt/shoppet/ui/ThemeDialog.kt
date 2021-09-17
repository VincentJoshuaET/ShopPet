package com.vt.shoppet.ui

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vt.shoppet.R
import com.vt.shoppet.util.PreferenceUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThemeDialog : DialogFragment() {

    @Inject
    lateinit var preferences: PreferenceUtils

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var item = preferences.getTheme()
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
                            preferences.setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        } else preferences.setTheme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    }
                    1 -> preferences.setTheme(AppCompatDelegate.MODE_NIGHT_NO)
                    2 -> preferences.setTheme(AppCompatDelegate.MODE_NIGHT_YES)
                }
                dialog.dismiss()
            }
            .create()
    }

}