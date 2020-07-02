package com.vt.shoppet.util

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

val permissions =
    arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

fun Fragment.checkSelfPermissions(): Boolean {
    val context = requireContext()
    val cameraPermission =
        context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val storagePermission =
        context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    return cameraPermission && storagePermission
}

fun MutableMap<String, Boolean>.checkAllPermissions(): Boolean {
    var permission = true
    for (entry in entries) {
        if (!entry.value) {
            permission = false
            break
        }
    }
    return permission
}

object PermissionUtils {
    const val SELECT_PHOTO = 1
    const val TAKE_PHOTO = 1001
}