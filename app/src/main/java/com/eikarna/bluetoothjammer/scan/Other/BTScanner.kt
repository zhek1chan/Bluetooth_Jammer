package com.eikarna.bluetoothjammer.scan.Other

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.eikarna.bluetoothjammer.scan.BTUtils
import com.eikarna.bluetoothjammer.scan.ViewModel.DevicesViewModel
import com.eikarna.bluetoothjammer.scan.Model.BTDevice
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("MissingPermission")
data class BTScanner(
	private val activity: ComponentActivity,
	private val scanTime: Long,
	private val signalStrength: Int,
	private val viewModel: DevicesViewModel,
	)
{
	private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private var isScanning: Boolean = false

	init
	{
		val bluetoothManager: BluetoothManager = this.activity
			.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
		bluetoothAdapter = bluetoothManager.adapter
		bluetoothAdapter.enable()
	}

	fun start()
	{
		if(BTUtils.checkBluetooth(bluetoothAdapter))
		{
			scan(true)
		}
		else
		{
			Log.d("Scanner", "No Bluetooth!!")
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	@SuppressLint("MissingPermission")
	private fun scan(enable: Boolean)
	{
		if(enable && !isScanning)
		{
			Log.d("Scanner", "Starting Scanning...")

			GlobalScope.launch(Dispatchers.Default) {
				delay(scanTime)
				isScanning = false
				bluetoothAdapter.stopLeScan(scanCallback)
			}

			isScanning = true
			bluetoothAdapter.startLeScan(scanCallback)
		}
	}

	@SuppressLint("MissingPermission", "SimpleDateFormat")
	private val scanCallback = BluetoothAdapter.LeScanCallback { device, rssi, _ ->
		val distance = BTUtils.rssiToDistance(rssi, signalStrength)
		val name = if(device.name != null)
		{
			device.name
		}
		else
		{
			"нет данных"
		}
		val calendar = Calendar.getInstance()
		val hour = calendar.get(Calendar.HOUR_OF_DAY)
		val minute = calendar.get(Calendar.MINUTE)
		val second = calendar.get(Calendar.SECOND)

		val timeString = String.format("%02d:%02d:%02d", hour, minute, second)
		val address = device.address
		val btDevice = BTDevice(
			name = name,
			distance = distance,
			mac = address,
			time = timeString
		)

		viewModel.addDevice(btDevice)
	}
}