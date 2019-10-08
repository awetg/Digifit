package com.bwet.digifit.utils

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

class RuntimePermissionUtil private constructor (private val activity: Activity) {

    // add all permissions here
    private val requiredPermissions = arrayOf<String>(
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE)
    }

    companion object : SingletonHolder<RuntimePermissionUtil, Activity>(::RuntimePermissionUtil) {
        const val PERMISSION_REQUEST_CODE = 1111
    }

    fun requestAllUnGrantedermissions() {
        val unGrantedPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        val permissionsWithReuestRationale = unGrantedPermissions.filter {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        requestPermissions(Array(permissionsWithReuestRationale.size) {permissionsWithReuestRationale[it]})
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun requestActivityRecognition() {
        requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION))
    }

    // An activityType requesting permission can listen to onRequestPermissionsResult and take further action with this method if needed
    fun showDialogAndAsk(message: String, onPositiveResponse: DialogInterface.OnClickListener?, onNegativeResponse: DialogInterface.OnClickListener? = null) {
        AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton("OK", onPositiveResponse)
            .setNegativeButton("No", onNegativeResponse)
            .setCancelable(false)
            .show()
    }

    fun isPermissionAvailable(permission: String): Boolean = ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
}