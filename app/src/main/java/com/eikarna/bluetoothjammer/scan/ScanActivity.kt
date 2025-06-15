package com.eikarna.bluetoothjammer.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import com.eikarna.bluetoothjammer.scan.Permissions.IPermissionsController
import com.eikarna.bluetoothjammer.scan.Permissions.PermissionsController
import com.eikarna.bluetoothjammer.scan.ViewModel.DevicesViewModel
import com.eikarna.bluetoothjammer.scan.components.FileSaver
import com.eikarna.bluetoothjammer.scan.components.HomeScreen

class ScanActivity :
    ComponentActivity(),
    IPermissionsController by PermissionsController() {
    private val viewModel: DevicesViewModel = DevicesViewModel(saveFile = { saveToFile() })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setPermissions()
        setComposeContent()
        setScanner()
    }

    /**
     * sets the compose
     */
    @OptIn(ExperimentalLayoutApi::class)
    private fun setComposeContent() {
        setContent {
            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBarsIgnoringVisibility)) {
                HomeScreen(viewModel = viewModel)
            }
        }
    }

    private fun setPermissions() {
        if (!checkPermissions()) {
            requestPermissions(this)
        }
    }

    private fun setScanner() {
        viewModel.setScanner(this)
    }

    private fun saveToFile() {
        val fileSaver = FileSaver(this)
        val linesList = mutableListOf<String>()
        val list = viewModel.devices.value.values.toList()
        for (i in list.indices) {
            linesList.add(list[i].toString())
        }
        fileSaver.saveLinesToFileAndShare(linesList, "found_devices_data")
    }
}


