package com.jucelio.rotacerta.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RotaColorScheme = darkColorScheme(
    primary = Color(0xFF16C784),
    onPrimary = Color(0xFF042015),
    secondary = Color(0xFF0FA36B),
    background = Color(0xFF0B0D11),
    onBackground = Color(0xFFEDEFF3),
    surface = Color(0xFF151821),
    onSurface = Color(0xFFEDEFF3),
    error = Color(0xFFFF5A5F)
)

@Composable
fun RotaCertaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RotaColorScheme,
        content = content
    )
}
