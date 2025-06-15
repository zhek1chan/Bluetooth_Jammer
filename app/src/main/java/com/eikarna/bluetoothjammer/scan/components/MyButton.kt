package com.eikarna.bluetoothjammer.scan.components

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yonigofman.bluetoothscannerapp.ui.theme.Green1
import com.yonigofman.bluetoothscannerapp.ui.theme.Green2

@Composable
fun MyButton(text:String = "click",onClick : () ->Unit) {
    
    Button(
        onClick = { onClick() },
        colors =ButtonDefaults.buttonColors(backgroundColor = Green1) ) {

        Text(
            text = text,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        
    }
    
}

@Composable
fun MyButtonSmall(text:String = "click",onClick : () ->Unit) {

    Button(
        onClick = { onClick() },
        colors =ButtonDefaults.buttonColors(backgroundColor = Green2) ) {

        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )


    }

}