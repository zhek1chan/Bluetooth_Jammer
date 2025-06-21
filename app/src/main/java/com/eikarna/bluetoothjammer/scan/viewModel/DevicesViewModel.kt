package com.eikarna.bluetoothjammer.scan.viewModel

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.eikarna.bluetoothjammer.scan.model.BTDevice
import com.eikarna.bluetoothjammer.scan.other.BTScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DevicesViewModel(val saveFile: () -> Unit) : ViewModel()
{
	private lateinit var scanner: BTScanner
	private var scanTime = 10000L
	private val _devices = MutableStateFlow<Map<String, BTDevice>>(emptyMap())
	val devices: StateFlow<Map<String, BTDevice>> = _devices.asStateFlow()
	fun addDevice(device: BTDevice)
	{
		val currentDevices = _devices.value.toMutableMap()

		if (!currentDevices.containsKey(device.mac))
		{
			currentDevices[device.mac] = device
		}
		else
		{
			currentDevices[device.mac]?.distance = device.distance
		}

		_devices.value = currentDevices // Обновляем StateFlow
		// Логирование (опционально)
		currentDevices.forEach {
			Log.d(
				"devices",
				"id: ${it.value.mac} | name: ${it.value.name} | distance ${it.value.distance}"
			)
		}
	}

	fun setScanTime(seconds: Long) {
		scanTime = seconds * 1000
	}

	fun getScanTime(): Long {
		return scanTime
	}

	fun startScan()
	{
		scanner.start()
	}

	fun setScanner(activity: ComponentActivity)
	{
		scanner = BTScanner(activity, scanTime, -200, this)
	}

	fun saveToFile() {
		saveFile()
	}
}