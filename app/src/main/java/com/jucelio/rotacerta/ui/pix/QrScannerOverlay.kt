package com.jucelio.rotacerta.ui.pix

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Moldura visual da área de leitura do scanner.
 * A sobreposição não intercepta eventos e mantém a câmera visível ao fundo.
 */
@Composable
fun QrScannerOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 190.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFF16C784),
                    shape = RoundedCornerShape(24.dp)
                )
        )
    }
}
