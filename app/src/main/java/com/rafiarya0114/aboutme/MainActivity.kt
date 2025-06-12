package com.rafiarya0114.aboutme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rafiarya0114.aboutme.ui.screen.MainScreen
import com.rafiarya0114.aboutme.ui.theme.AboutMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AboutMeTheme {
                MainScreen()
            }
        }
    }
}