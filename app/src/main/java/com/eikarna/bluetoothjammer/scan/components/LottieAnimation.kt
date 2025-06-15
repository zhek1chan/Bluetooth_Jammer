package com.eikarna.bluetoothjammer.scan.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eikarna.bluetoothjammer.R

@Composable
fun LottieScanAnimation(isPlaying: Boolean) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan_animation3))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = isPlaying,
        iterations = LottieConstants.IterateForever,
        speed = 1f,
    )
    LottieAnimation(composition = composition, progress = { progress })
}