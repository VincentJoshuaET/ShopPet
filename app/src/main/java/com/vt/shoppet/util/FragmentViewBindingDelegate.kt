package com.vt.shoppet.util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val factory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var _binding: T? = null

    init {
        fragment.run {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onCreate(lifecycleOwner: LifecycleOwner) {
                    viewLifecycleOwnerLiveData.observe(fragment) { owner ->
                        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                            override fun onDestroy(owner: LifecycleOwner) {
                                _binding = null
                            }
                        })
                    }
                }
            })
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = _binding
        if (binding != null) return binding

        val currentState = fragment.viewLifecycleOwner.lifecycle.currentState
        if (!currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return factory(thisRef.requireView()).also { _binding = it }
    }
}