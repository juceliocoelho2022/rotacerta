package com.jucelio.rotacerta.ui.delivery

import androidx.compose.ui.graphics.Color

/**
 * Paleta do RotaCerta (modo entregador).
 *
 * Dark grafite neutro com o verde usado apenas como acento
 * (status, progresso, ações principais). O vermelho do iFood
 * identifica a categoria da entrega.
 */
internal object RotaColors {
    val Background = Color(0xFF0B0D11)
    val Surface = Color(0xFF151821)
    val Surface2 = Color(0xFF1B1F2A)
    val Line = Color(0x14FFFFFF)          // branco ~8%
    val Accent = Color(0xFF16C784)        // verde
    val Accent2 = Color(0xFF0FA36B)
    val OnAccent = Color(0xFF042015)
    val IFood = Color(0xFFEA1D2C)
    val Danger = Color(0xFFFF5A5F)
    val White = Color(0xFFEDEFF3)
    val Secondary = Color(0xFF9298A5)
}
