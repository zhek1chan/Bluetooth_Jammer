package com.eikarna.bluetoothjammer.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import com.eikarna.bluetoothjammer.scan.components.FileSaver
import com.eikarna.bluetoothjammer.scan.components.HomeScreen
import com.eikarna.bluetoothjammer.scan.permissions.IPermissionsController
import com.eikarna.bluetoothjammer.scan.permissions.PermissionsController
import com.eikarna.bluetoothjammer.scan.viewModel.DevicesViewModel


class ScanActivity :
    ComponentActivity(),
    IPermissionsController by PermissionsController() {
    private val viewModel: DevicesViewModel = DevicesViewModel(saveFile = { saveToFile() })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setPermissions()
    }

    override fun onResume() {
        super.onResume()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // Bluetooth is either not supported or not enabled, show dialog
            showBluetoothDisabledDialog()
        } else {
            setComposeContent()
            setScanner()
        }
    }

    private fun showBluetoothDisabledDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage("Bluetooth выключен")
            .setTitle("Пожалуйста включите его в настройках")
            .setPositiveButton("Go to settings") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(
                    this,
                    "Пока вы не включите Bluetooth,\nприложение не будет работать",
                    Toast.LENGTH_SHORT
                ).show()
                this.onResume()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
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


