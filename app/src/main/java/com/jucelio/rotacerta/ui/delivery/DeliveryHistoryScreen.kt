package com.jucelio.rotacerta.ui.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jucelio.rotacerta.domain.model.delivery.Delivery
import com.jucelio.rotacerta.domain.model.delivery.DeliveryStatus

/**
 * Histórico de entregas concluídas na sessão atual do entregador.
 */
@Composable
fun DeliveryHistoryScreen(
    state: DeliveryUiState,
    onScanClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRouteClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val history = state.history

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = RotaColors.Background,
        bottomBar = {
            DeliveryBottomBar(
                current = DeliveryTab.HISTORY,
                onHomeClick = onHomeClick,
                onRouteClick = onRouteClick,
                onScanClick = onScanClick,
                onHistoryClick = {},
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->

        if (history.isEmpty()) {
            EmptyHistory(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hoje",
                        modifier = Modifier.weight(1f),
                        color = RotaColors.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sessão atual",
                        color = RotaColors.Secondary,
                        fontSize = 12.5.sp
                    )
                }
            }

            item {
                SummaryCard(state = state)
            }

            items(
                items = history,
                key = { it.id }
            ) { delivery ->
                HistoryItem(delivery = delivery)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    state: DeliveryUiState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1C212C), Color(0xFF141821))
                )
            )
            .border(1.dp, RotaColors.Line, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 4.dp, height = 44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(RotaColors.Accent, RotaColors.Accent2)
                    )
                )
        )

        Row(modifier = Modifier.padding(start = 12.dp)) {
            SummaryItem("Entregues", state.deliveredStops.toString())
            SummaryItem("Não entregues", state.failedStops.toString())
            SummaryItem("Total", state.history.size.toString())
        }
    }
}

@Composable
private fun RowScope.SummaryItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = RotaColors.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = RotaColors.Secondary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HistoryItem(
    delivery: Delivery
) {
    val delivered = delivery.status == DeliveryStatus.DELIVERED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RotaColors.Surface)
            .border(1.dp, RotaColors.Line, RoundedCornerShape(16.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (delivered) {
                        RotaColors.Accent.copy(alpha = 0.14f)
                    } else {
                        RotaColors.Danger.copy(alpha = 0.14f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (delivered) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (delivered) RotaColors.Accent else RotaColors.Danger,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = delivery.recipientName,
                color = RotaColors.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${delivery.street} · ${delivery.type.label}",
                color = RotaColors.Secondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Text(
            text = if (delivered) "Entregue" else "Não entregue",
            color = if (delivered) RotaColors.Accent else RotaColors.Danger,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyHistory(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = RotaColors.Accent,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sem entregas concluídas",
            color = RotaColors.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "As entregas finalizadas aparecem aqui.",
            color = RotaColors.Secondary,
            fontSize = 14.sp
        )
    }
}
