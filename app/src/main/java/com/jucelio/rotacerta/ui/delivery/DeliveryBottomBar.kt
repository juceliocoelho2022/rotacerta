package com.jucelio.rotacerta.ui.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Abas da navegação inferior do modo entregador. */
internal enum class DeliveryTab { HOME, ROUTE, HISTORY, PROFILE }

/**
 * Barra de navegação inferior do RotaCerta, com o botão central
 * de leitura em destaque.
 */
@Composable
internal fun DeliveryBottomBar(
    current: DeliveryTab,
    onHomeClick: () -> Unit,
    onRouteClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RotaColors.Background)
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 10.dp,
                bottom = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NavItem(
            icon = Icons.Default.Home,
            label = "Início",
            selected = current == DeliveryTab.HOME,
            onClick = onHomeClick
        )

        NavItem(
            icon = Icons.Default.Route,
            label = "Rota",
            selected = current == DeliveryTab.ROUTE,
            onClick = onRouteClick
        )

        ScanButton(onClick = onScanClick)

        NavItem(
            icon = Icons.Default.ReceiptLong,
            label = "Histórico",
            selected = current == DeliveryTab.HISTORY,
            onClick = onHistoryClick
        )

        NavItem(
            icon = Icons.Default.Person,
            label = "Perfil",
            selected = current == DeliveryTab.PROFILE,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) RotaColors.Accent else RotaColors.Secondary

    Column(
        modifier = Modifier
            .size(width = 58.dp, height = 48.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(23.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ScanButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(y = (-14).dp)
            .size(58.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        RotaColors.Accent,
                        RotaColors.Accent2
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Ler endereço",
            tint = RotaColors.OnAccent,
            modifier = Modifier.size(27.dp)
        )
    }
}
