package com.eikarna.bluetoothjammer.scan.components

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eikarna.bluetoothjammer.R
import com.eikarna.bluetoothjammer.scan.ViewModel.DevicesViewModel
import com.yonigofman.bluetoothscannerapp.ui.theme.Green1
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesList(viewModel: DevicesViewModel) {
    val devices by viewModel.devices.collectAsState()
    val sortedDevices = remember(devices) {
        devices.values.sortedBy { it.distance }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var startWasPressed by remember { mutableStateOf(false) }
    var filterWasPressed by remember { mutableStateOf(false) }
    var numericValue by rememberSaveable { mutableStateOf("") }
	val context = LocalContext.current
    TopAppBar(
        title = {
            Text(
                text = "Экран сканирования",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            IconButton(onClick = {(context as Activity).onBackPressed() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back), // Ваша иконка "назад"
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent, // Прозрачный фон
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(5.dp))
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!startWasPressed) {
            MyButton("Старт") {
                if (numericValue.isNotEmpty()) {
                    viewModel.setScanTime(numericValue.toLong())
                }
                viewModel.startScan()
                isPlaying = true
                startWasPressed = true
            }
        }
        val text = if (!filterWasPressed) {
            "Показать только с названием"
        } else {
            "Показать все"
        }

        var progress by remember { mutableFloatStateOf(0f) }

        if (isPlaying) {
            LaunchedEffect(startWasPressed) {
                // Запускаем прогресс одновременно со сканированием
                loadProgress(viewModel.getScanTime()) { newProgress ->
                    progress = newProgress
                }
            }
            LaunchedEffect(startWasPressed) {
                // Ждём завершения сканирования
                var timeL = viewModel.getScanTime() / 1000
                while (timeL > 0) {
                    delay(1.seconds)
                    timeL--
                    println(timeL)
                }
                isPlaying = false
            }

            LottieScanAnimation(isPlaying = true)

            LinearProgressIndicator(
                modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 8.dp),
                progress = progress,
                color = Color.Green,
                backgroundColor = Color.Black
            )
        }

        if (!startWasPressed) {

            Text(
                text = "Без ввода значения будет выставлено 10 секунд",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            NumericInputField(
                value = numericValue,
                onValueChange = { numericValue = it },
                label = { Text("Введите время сканирования в секундах", color = Color.White) },
                placeholder = { Text("Только цифры") }
            )
        } else {
            Text(
                text = "Время поиска - ${viewModel.getScanTime() / 1000}c",
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        val displayedDevices = remember(filterWasPressed, sortedDevices) {
            if (filterWasPressed) {
                sortedDevices.filter { it.name != "нет данных" }
            } else {
                sortedDevices
            }
        }

        if (!isPlaying && startWasPressed) {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                Toast.makeText(
                    context,
                    "Сканирование завершено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Row(
                modifier = Modifier
					.padding(top = 15.dp)
					.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MyButtonSmall(text) {
                    filterWasPressed = !filterWasPressed
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp)
            ) {
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(Green1),
                    onClick = {
                        viewModel.saveToFile()
                    },
                ) {
                    Text("Сохранить результата поиска в файл", color = Color.White)
                    Icon(
                        modifier = Modifier.padding(start = 5.dp),
                        painter = painterResource(R.drawable.download),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayedDevices) { device ->
                DeviceItem(device = device)
            }
        }
    }
}

@Composable
fun NumericInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = { newText ->
            val filteredText = newText.filter { it.isDigit() }
            if (filteredText != newText || newText.isEmpty()) {
                onValueChange(filteredText)
            } else {
                onValueChange(newText)
            }
        },
        modifier = modifier.padding(8.dp),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.Gray
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true,
        label = label,
        placeholder = placeholder
    )
}


suspend fun loadProgress(time: Long, updateProgress: (Float) -> Unit) {
    val startTime = System.currentTimeMillis()
    val endTime = startTime + time
    Log.d("check", "$time")
    Log.d("check", "startTime = $startTime")
    Log.d("check", "endTime = $endTime")

    while (System.currentTimeMillis() < endTime) {
        val elapsed = System.currentTimeMillis() - startTime
        val progress = (elapsed.toFloat() / time).coerceIn(0f, 1f)
        updateProgress(progress)
        delay(16) // ~60 updates per second for smooth animation
    }
    // Ensure we finish at 100%
    updateProgress(1f)
}


