package com.vt.shoppet.util

import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import com.vt.shoppet.util.PermissionUtils.CAMERA
import com.vt.shoppet.util.PermissionUtils.WRITE_EXTERNAL_STORAGE

val permissions =
    arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)

val Fragment.checkSelfPermissions: Boolean
    get() = requireContext().run {
        checkSelfPermission(CAMERA) == PERMISSION_GRANTED &&
                checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
    }

val MutableMap<String, Boolean>.checkAllPermissions: Boolean
    get() {
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
    const val CAMERA = android.Manifest.permission.CAMERA
    const val WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val SELECT_PHOTO = 1
    const val TAKE_PHOTO = 1001
}