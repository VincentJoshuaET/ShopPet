package com.vt.shoppet.util

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

fun <T : ViewBinding> Fragment.viewBinding(factory: (View) -> T) =
    FragmentViewBindingDelegate(this, factory)

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline inflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        inflater.invoke(layoutInflater)
    }