package com.eikarna.bluetoothjammer.scan.permissions

import androidx.activity.ComponentActivity

interface IPermissionsController {
    fun checkPermissions():Boolean
    fun requestPermissions(activity:ComponentActivity)
}