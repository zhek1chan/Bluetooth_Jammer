package com.eikarna.bluetoothjammer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.widget.doAfterTextChanged
import com.eikarna.bluetoothjammer.api.L2capFloodAttack
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class AttackActivity : AppCompatActivity() {
    // UI elements
    private lateinit var viewDeviceName: MaterialTextView
    private lateinit var viewDeviceAddress: MaterialTextView
    private lateinit var viewThreads: TextInputEditText
    private lateinit var buttonStartStop: MaterialButton
    private lateinit var logAttack: MaterialTextView
    private lateinit var switchLog: MaterialSwitch

    // Attack state
    private lateinit var deviceName: String
    private lateinit var address: String
    private var threads: Int = DEFAULT_THREAD_COUNT
    private var activeAttacks = mutableListOf<L2capFloodAttack>()
    private var bluetoothAdapter: BluetoothAdapter? = null

    companion object {
        @JvmStatic
        var isAttacking = false
        const val FrameworkVersion = "1.1"
        var loggingStatus = true
        private const val DEFAULT_THREAD_COUNT = 1
        private const val MAX_LOG_LINES = 100
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attack_layout)
        setupBluetooth()
        initViews()
        setupEventListeners()
        logFrameworkVersion()
    }

    private fun setupBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter().also {
            if (it == null) {
                showToast("Bluetooth не доступен")
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initViews() {
        viewDeviceName = findViewById(R.id.textViewDeviceName)
        viewDeviceAddress = findViewById(R.id.textViewAddress)
        viewThreads = findViewById(R.id.editTextThreads)
        buttonStartStop = findViewById(R.id.buttonStartStop)
        logAttack = findViewById(R.id.logTextView)
        switchLog = findViewById(R.id.switchLogView)

        // Initialize from intent
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Unknown Device"
        address = intent.getStringExtra("ADDRESS") ?: "Unknown Address"
        threads = intent.getIntExtra("THREADS", DEFAULT_THREAD_COUNT)

        // Set initial values
        viewDeviceName.text = "Название: $deviceName"
        viewDeviceAddress.text = "Адрес: $address"
        viewThreads.setText(threads.toString())
        logAttack.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun setupEventListeners() {
        buttonStartStop.setOnClickListener {
            if (isAttacking) stopAttack() else startAttack()
        }

        viewThreads.doAfterTextChanged { text ->
            text?.toString()?.takeIf { it.isNotEmpty() && it.isDigitsOnly() }?.let {
                threads = it.toInt().coerceAtLeast(1)
            }
        }

        switchLog.setOnCheckedChangeListener { _, isChecked ->
            loggingStatus = isChecked
            showToast(if (isChecked) "Логирование включено" else "Логирование выключено")
        }
    }

    private fun logFrameworkVersion() {
        appendLog("Bluetooth Jammer v$FrameworkVersion")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startAttack() {
        if (!checkBluetoothPermissions()) {
            showToast("Bluetooth permissions required!")
            return
        }

        isAttacking = true
        buttonStartStop.text = "Стоп"
        bluetoothAdapter?.cancelDiscovery()
        activeAttacks.clear()

        appendLog("Attack started on $deviceName ($address) with $threads threads")
        showToast("Attack running - use STOP button to end")

        repeat(threads) {
            L2capFloodAttack(address).apply {
                startAttack(this@AttackActivity, logAttack)
                activeAttacks.add(this)
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopAttack() {
        isAttacking = false
        buttonStartStop.text = "Старт"

        activeAttacks.forEach { it.stopAttack() }
        activeAttacks.clear()

        bluetoothAdapter?.startDiscovery()
        appendLog("Атака была остановлена")
    }

    private fun appendLog(message: String) {
        if (!loggingStatus) return

        runOnUiThread {
            val currentLogs = logAttack.text.toString()
            val newLogs = if (currentLogs.isEmpty()) message else "$currentLogs\n$message"

            // Limit log size
            val lines = newLogs.split('\n')
            logAttack.text = if (lines.size > MAX_LOG_LINES) {
                lines.takeLast(MAX_LOG_LINES).joinToString("\n")
            } else {
                newLogs
            }
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        if (isAttacking) stopAttack()
        super.onDestroy()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onPause() {
        if (isAttacking) stopAttack()
        super.onPause()
    }
}