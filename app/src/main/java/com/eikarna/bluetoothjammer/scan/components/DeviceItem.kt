package com.eikarna.bluetoothjammer.scan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eikarna.bluetoothjammer.scan.Model.BTDevice
import com.yonigofman.bluetoothscannerapp.ui.theme.Background1
import com.yonigofman.bluetoothscannerapp.ui.theme.Green1
import com.yonigofman.bluetoothscannerapp.ui.theme.Green2
import com.yonigofman.bluetoothscannerapp.ui.theme.Shapes
import kotlin.random.Random

@Preview
@Composable
fun DeviceItem(device: BTDevice = defaultDevice()) {
    Card(
        backgroundColor = Background1,
        elevation = 15.dp,
        shape = Shapes.small,
        border = BorderStroke(2.dp, Green2)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Имя: ${device.name}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "MAC: ${device.mac}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Примерная дистанция: ${device.distance}м",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Время обнаружения: ${device.time}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (device.distance <= 1) {
                Text(
                    text = "Хороший сигнал",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green1
                )
            } else {
                Text(
                    text = "Слабый сигнал",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

fun defaultDevice(): BTDevice {
    return BTDevice(
        name = "UnK",
        distance = Random.nextDouble(30.0),
        mac = "${Random.nextInt(0, 9)}A: " +
                "C:${Random.nextInt(0, 9)}" +
                ":${Random.nextInt(0, 9)}G",
        time = "22.11.2222"
    )
}
