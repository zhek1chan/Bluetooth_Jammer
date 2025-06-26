package com.eikarna.bluetoothjammer.scan

import android.bluetooth.BluetoothAdapter
import java.text.DecimalFormat
import kotlin.math.pow

object BTUtils {


    fun checkBluetooth(bluetoothAdapter: BluetoothAdapter?):Boolean{
        return !(bluetoothAdapter == null || !bluetoothAdapter.isEnabled)
    }


    fun rssiToDistance(rssi: Int,signalStrength:Int): Double {
        return 10.0.pow((1.0 * (signalStrength - rssi)) / (10 * 2)).format(2)
    }

   private fun Double.format(fracDigits: Int): Double {
        val df = DecimalFormat()
        df.maximumFractionDigits = fracDigits
        return df.format(this).replace(',','.').toDouble()
    }




}