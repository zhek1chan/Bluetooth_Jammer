package com.eikarna.bluetoothjammer.scan.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionsController : IPermissionsController {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val permissionsMap: MutableMap<String, Boolean> = mutableMapOf()
    private val permissionRequest: MutableList<String> = ArrayList()

    override fun checkPermissions(): Boolean {
        return permissionsMap.all { it.value }
    }

    override fun requestPermissions(activity: ComponentActivity) {
        setLaunchPermissionRequest(activity)
        setupPermissionsForApiLevel(activity)

        permissionsMap.forEach {
            permissionsMap.replace(it.key, isPermissionGranted(activity, it.key))
        }

        val deniedMap = permissionsMap.filter { !it.value }
        Log.d("Permissions", "Denied permissions: $deniedMap")

        deniedMap.forEach { addPermissionToRequest(it.key) }
        Log.d("Permissions", "Requesting permissions: $permissionRequest")

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun setupPermissionsForApiLevel(activity: ComponentActivity) {
        permissionsMap.clear()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsMap[Manifest.permission.BLUETOOTH_SCAN] = false
            permissionsMap[Manifest.permission.BLUETOOTH_CONNECT] = false

            if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
                permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] = false
            }
        } else {
            permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] = false
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsMap[Manifest.permission.BLUETOOTH] = false
            permissionsMap[Manifest.permission.BLUETOOTH_ADMIN] = false
        }
    }

    private fun addPermissionToRequest(permission: String) {
        if (!permissionRequest.contains(permission)) {
            this.permissionRequest.add(permission)
        }
    }

    private fun isPermissionGranted(activity: ComponentActivity, permission: String) =
        ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    private fun setLaunchPermissionRequest(activity: ComponentActivity) {
        permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach { entry ->
                    permissionsMap[entry.key] = entry.value
                }
                Log.d("Permissions", "Updated permissions after request: $permissionsMap")
            }
    }
}