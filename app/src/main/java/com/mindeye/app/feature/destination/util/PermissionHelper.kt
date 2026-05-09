package com.mindeye.app.feature.destination.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限申请辅助工具类
 */
object PermissionHelper {

    const val REQ_CODE_AUDIO = 1001
    const val REQ_CODE_LOCATION = 1002

    /**
     * 检查录音权限
     */
    fun hasAudioPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查定位权限
     */
    fun hasLocationPermission(activity: Activity): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    /**
     * 申请录音权限
     */
    fun requestAudioPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQ_CODE_AUDIO
        )
    }

    /**
     * 申请定位权限
     */
    fun requestLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQ_CODE_LOCATION
        )
    }
}
