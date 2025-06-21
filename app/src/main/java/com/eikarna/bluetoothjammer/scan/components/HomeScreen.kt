package com.eikarna.bluetoothjammer.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eikarna.bluetoothjammer.scan.viewModel.DevicesViewModel
import com.yonigofman.bluetoothscannerapp.ui.theme.Background2

@Composable
fun HomeScreen(
	modifier: Modifier = Modifier,
	viewModel: DevicesViewModel
)
{
	Column(modifier = modifier
		.fillMaxSize()
		.background(Background2),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		DevicesList(viewModel)
	}
}