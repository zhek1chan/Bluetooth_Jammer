package com.eikarna.bluetoothjammer.scan.model

data class BTDevice(
	val name: String = "",
	val mac: String,
	var distance: Double,
	val time: String
)
