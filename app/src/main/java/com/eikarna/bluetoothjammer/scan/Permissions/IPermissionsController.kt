package com.eikarna.bluetoothjammer.scan.Permissions

import androidx.activity.ComponentActivity

interface IPermissionsController {
    fun checkPermissions():Boolean
    fun requestPermissions(activity:ComponentActivity)
}