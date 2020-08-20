package com.vt.shoppet.util

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.util.TypedValue
import android.view.View
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.vt.shoppet.R
import com.vt.shoppet.databinding.ActivityMainBinding

private fun Lifecycle.observeSnackbar(snackbar: Snackbar) =
    addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            snackbar.dismiss()
        }
    })

val Fragment.circularProgress: Animatable
    get() {
        val value = TypedValue()
        val context = requireContext()
        context.theme.resolveAttribute(android.R.attr.progressBarStyleSmall, value, false)
        val array = intArrayOf(android.R.attr.indeterminateDrawable)
        val attributes = context.obtainStyledAttributes(value.data, array)
        val drawable = attributes.getDrawable(0)
        attributes.recycle()
        return drawable as Animatable
    }

val Fragment.circularProgressLarge: CircularProgressDrawable
    get() = CircularProgressDrawable(requireContext()).apply {
        setStyle(CircularProgressDrawable.LARGE)
        setColorSchemeColors(Color.WHITE)
        start()
    }

fun ActivityMainBinding.showSnackbar(
    message: String?,
    @StringRes id: Int = R.string.btn_retry,
    action: ((View) -> Unit)? = null
) = Snackbar.make(fragment, message ?: "Empty Message", Snackbar.LENGTH_SHORT).apply {
    if (action != null) setAction(id, action)
}.show()

fun <T : ViewBinding> T.snackbar(
    message: String?,
    anchor: View? = null,
    gravity: Int? = null,
    owner: LifecycleOwner? = null,
    action: ((View) -> Unit)? = null
) = Snackbar.make(root, message ?: "Empty Message", Snackbar.LENGTH_SHORT).apply {
    if (anchor != null) anchorView = anchor
    if (gravity != null) {
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        view.layoutParams = params.apply { this.gravity = gravity }
    }
    if (action != null) setAction(R.string.btn_retry, action)
    owner?.lifecycle?.observeSnackbar(this)
}

fun Fragment.setLayout(orientation: Int) = when (orientation) {
    Configuration.ORIENTATION_LANDSCAPE -> GridLayoutManager(context, 3)
    else -> GridLayoutManager(context, 2)
}

fun RecyclerView.setOnLayoutChangeListener() =
    addOnLayoutChangeListener { _, _, top, _, _, _, oldTop, _, _ ->
        if (top < oldTop) smoothScrollToPosition(oldTop)
    }

fun ActivityMainBinding.setupAuthView() =
    apply {
        appbar.isVisible = false
        bottomNavigationView.isVisible = false
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

fun ActivityMainBinding.setupHomeNavigationView() =
    apply {
        appbar.isVisible = true
        bottomNavigationView.isVisible = true
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toolbar.menu.clear()
    }

fun ActivityMainBinding.setupToolbar(@MenuRes menu: Int? = null) =
    apply {
        appbar.isVisible = true
        bottomNavigationView.isVisible = false
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.menu.clear()
        if (menu != null) toolbar.inflateMenu(menu)
    }