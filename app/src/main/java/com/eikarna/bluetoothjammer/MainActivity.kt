package com.eikarna.bluetoothjammer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eikarna.bluetoothjammer.api.BluetoothDeviceInfo
import com.eikarna.bluetoothjammer.api.ScanNearbyDevices
import com.eikarna.bluetoothjammer.scan.ScanActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private lateinit var devices: List<BluetoothDeviceInfo>
    private val scanner = ScanNearbyDevices.getInstance()

    companion object {
        const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.deviceListView)
        val button = findViewById<ImageView>(R.id.infoButton)
        button.setOnClickListener {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }
        // Check and request necessary permissions
        checkBluetoothStatusAndPermissions()
        findViewById<FloatingActionButton>(R.id.scanButton).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun checkBluetoothStatusAndPermissions() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // Bluetooth is either not supported or not enabled, show dialog
            showBluetoothDisabledDialog()
        } else {
            // Bluetooth is enabled, proceed with permission checks
            checkPermissionsAndStartScanning()
        }
    }

    @SuppressLint("MissingPermission")
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
                checkBluetoothStatusAndPermissions()
                Toast.makeText(
                    this,
                    "Пока вы не включите Bluetooth,\nприложение не будет работать",
                    Toast.LENGTH_SHORT
                ).show()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun checkPermissionsAndStartScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 and higher, request specific Bluetooth permissions
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (!hasPermissions(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            } else {
                // Permissions already granted, start scanning
                startScanningForDevices()
            }
        } else {
            // For older Android versions, request Bluetooth and location permissions
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (!hasPermissions(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            } else {
                // Permissions already granted, start scanning
                startScanningForDevices()
            }
        }
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions were granted
                startScanningForDevices()
            } else {
                // Permission denied, show a message
                Toast.makeText(
                    this,
                    "Permissions are required to scan for Bluetooth devices",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startScanningForDevices() {
        // Start scanning for nearby Bluetooth devices
        scanner.startScanning(this) { discoveredDevices ->
            devices = discoveredDevices
            val deviceNames = devices.map { "${it.name} (${it.address})" }

            // Set up ArrayAdapter to show the list of devices
            deviceListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
            listView.adapter = deviceListAdapter

            // Handle item click events
            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedDevice = devices[position]
                showDeviceInfo(selectedDevice)
            }
        }
    }

    // Show device details in a dialog
    private fun showDeviceInfo(device: BluetoothDeviceInfo) {
        val message =
            "Название: ${device.name}\nАдрес: ${device.address}"

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Информация об устройстве")
            .setMessage(message)
            .setPositiveButton("Атаковать") { dialog, _ ->
                dialog.dismiss()
                scanner.stopScanning()
                val intent = Intent(this, AttackActivity::class.java).apply {
                    putExtra("DEVICE_NAME", device.name)
                    putExtra("ADDRESS", device.address)
                    putExtra("THREADS", 8)
                }

                // Start AttackActivity
                startActivity(intent)
            }
            .setNegativeButton("Закрыть") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Скопировать") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("Device Info", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this,
                    "Информация об устройстве была скопирована",
                    Toast.LENGTH_SHORT
                ).show()
            }
        dialogBuilder.create().show()
    }

    // Stop scanning when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        scanner.stopScanning()
    }

    // Stop scanning when change to another intent
    override fun onPause() {
        super.onPause()
        scanner.stopScanning()
    }

    // Resume scanning
    override fun onResume() {
        super.onResume()
        checkBluetoothStatusAndPermissions()
    }
}
