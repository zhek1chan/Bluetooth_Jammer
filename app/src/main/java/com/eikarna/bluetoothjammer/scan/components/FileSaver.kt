package com.eikarna.bluetoothjammer.scan.components

import android.app.Activity
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileSaver(private val activity: Activity) {

    fun saveLinesToFileAndShare(lines: List<String>, fileNamePrefix: String = "data") {
        try {
            // Создаем временный файл
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${fileNamePrefix}_$timeStamp.txt"

            val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val outputFile = File(storageDir, fileName)

            // Записываем данные построчно
            FileWriter(outputFile).use { writer ->
                writer.write("BluetoothJammer log of founded devices:\n")
                lines.forEach { line ->
                    writer.write(line + "\n")
                }
            }

            // Предлагаем сохранить файл
            shareFile(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/plain"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(Intent.createChooser(shareIntent, "Save file"))
    }
}