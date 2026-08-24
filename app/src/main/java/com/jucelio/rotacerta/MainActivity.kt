package com.jucelio.rotacerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jucelio.rotacerta.presentation.navigation.RotaCertaApp
import com.jucelio.rotacerta.ui.theme.RotaCertaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RotaCertaTheme {
                RotaCertaApp()
            }
        }
    }
}
