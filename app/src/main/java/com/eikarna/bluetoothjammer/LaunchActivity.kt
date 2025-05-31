package com.eikarna.bluetoothjammer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eikarna.bluetoothjammer.MainActivity.Companion.PERMISSION_REQUEST_CODE
import com.eikarna.bluetoothjammer.databinding.ActivityLaunchBinding
import java.lang.ref.WeakReference


@SuppressLint("CustomSplashScreen")
class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val blackColor = ContextCompat.getColor(this, R.color.black)
        val whiteColor = ContextCompat.getColor(this, R.color.white)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(lightScrim = whiteColor, darkScrim = blackColor),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = whiteColor,
                darkScrim = blackColor
            )
        )
        val binding = ActivityLaunchBinding.inflate(layoutInflater)
        bindingRef = WeakReference(binding)
        setContentView(binding.root)
        binding.centerButton.setOnClickListener{
            checkBluetoothStatusAndPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showBluetoothDisabledDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage("Bluetooth is Off")
            .setTitle("Please turn on Bluetooth\nin your divice settings")
            .setPositiveButton("Go to settings") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                finishActivity(0)
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
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
                startActivity(Intent(this, MainActivity::class.java))
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
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if(hasPermissions(permissions)) {
            startActivity(Intent(this, MainActivity::class.java))
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

    override fun onDestroy() {
        bindingRef = null
        super.onDestroy()
    }

    companion object {
        private var bindingRef: WeakReference<ActivityLaunchBinding>? = null
    }

}