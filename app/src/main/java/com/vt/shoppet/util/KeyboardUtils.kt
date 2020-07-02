package com.vt.shoppet.util

import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyboardUtils @Inject constructor(private val inputMethodManager: InputMethodManager) {

    fun hide(fragment: Fragment) =
        inputMethodManager.hideSoftInputFromWindow(fragment.requireView().applicationWindowToken, 0)

}