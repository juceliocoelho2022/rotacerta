package com.jucelio.rotacerta.ui.delivery

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jucelio.rotacerta.domain.model.delivery.Delivery
import com.jucelio.rotacerta.domain.model.delivery.DeliveryType

/**
 * Tela inicial do modo entregador (RotaCerta).
 *
 * Mostra o status de disponibilidade, o resumo da rota do dia, o
 * atalho de leitura de endereço e a próxima parada.
 */
@Composable
fun DeliveryHomeScreen(
    state: DeliveryUiState,
    userName: String = "Jucelio",
    onToggleOnline: () -> Unit,
    onScanClick: () -> Unit,
    onRouteClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = RotaColors.Background,
        bottomBar = {
            DeliveryBottomBar(
                current = DeliveryTab.HOME,
                onHomeClick = {},
                onRouteClick = onRouteClick,
                onScanClick = onScanClick,
                onHistoryClick = onHistoryClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Header(
                    userName = userName,
                    isOnline = state.isOnline,
                    onToggleOnline = onToggleOnline
                )
            }

            item {
                RouteOfDayCard(
                    state = state,
                    onContinue = onRouteClick
                )
            }

            item {
                ScanRow(onClick = onScanClick)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próxima parada",
                        modifier = Modifier.weight(1f),
                        color = RotaColors.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Ver rota",
                        color = RotaColors.Accent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onRouteClick)
                    )
                }
            }

            item {
                val next = state.nextStop
                if (next == null) {
                    EmptyNextStop(onScanClick = onScanClick)
                } else {
                    NextStopCard(
                        delivery = next,
                        onOpenMap = { openInMaps(context, next) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    userName: String,
    isOnline: Boolean,
    onToggleOnline: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(RotaColors.Surface2)
                .border(1.dp, RotaColors.Line, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(2).uppercase(),
                color = RotaColors.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bora entregar?",
                color = RotaColors.Secondary,
                fontSize = 13.sp
            )
            Text(
                text = "Olá, $userName 👋",
                color = RotaColors.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
        }

        OnlinePill(
            isOnline = isOnline,
            onClick = onToggleOnline
        )
    }
}

@Composable
private fun OnlinePill(
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isOnline) RotaColors.Accent else RotaColors.Secondary,
        label = "onlineColor"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = if (isOnline) "Online" else "Offline",
            color = color,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RouteOfDayCard(
    state: DeliveryUiState,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1C212C),
                        Color(0xFF141821)
                    )
                )
            )
            .border(1.dp, RotaColors.Line, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        // Faixa de acento à esquerda.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(vertical = 2.dp)
                .size(width = 4.dp, height = 96.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(RotaColors.Accent, RotaColors.Accent2)
                    )
                )
        )

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "Rota de hoje",
                color = RotaColors.Secondary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.pendingStops.toString(),
                    color = RotaColors.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "paradas restantes",
                    color = RotaColors.Secondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MiniStat("Entregues", state.deliveredStops.toString())
                MiniStat("Pendentes", state.pendingStops.toString())
                MiniStat("Paradas", state.totalStops.toString())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progresso.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x17FFFFFF))
            ) {
                if (state.progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(state.progress)
                            .height(7.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(RotaColors.Accent2, RotaColors.Accent)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.deliveredStops} de ${state.totalStops} concluídas",
                    modifier = Modifier.weight(1f),
                    color = RotaColors.Secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RotaColors.Accent)
                        .clickable(onClick = onContinue)
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = RotaColors.OnAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Continuar",
                        color = RotaColors.OnAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.MiniStat(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(RotaColors.Surface2)
            .border(1.dp, RotaColors.Line, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 11.dp)
    ) {
        Text(
            text = value,
            color = RotaColors.White,
            fontSize = 16.sp,
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
private fun ScanRow(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RotaColors.Surface)
            .border(1.dp, RotaColors.Line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(RotaColors.Accent.copy(alpha = 0.12f))
                .border(1.dp, RotaColors.Accent.copy(alpha = 0.28f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = RotaColors.Accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(15.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Ler endereço da entrega",
                color = RotaColors.White,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "QR Code ou código de barras",
                color = RotaColors.Secondary,
                fontSize = 12.5.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = RotaColors.Accent,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun NextStopCard(
    delivery: Delivery,
    onOpenMap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RotaColors.Surface)
            .border(1.dp, RotaColors.Line, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RotaColors.Surface2)
                    .border(1.dp, RotaColors.Line, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "1",
                    color = RotaColors.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                DeliveryKindTag(delivery.type)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = delivery.recipientName,
                    color = RotaColors.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = delivery.fullAddress,
                    color = RotaColors.Secondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                delivery.orderCode?.let { code ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RotaColors.Accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Pedido $code",
                            color = RotaColors.Secondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(13.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RotaColors.Accent)
                .clickable(onClick = onOpenMap),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = RotaColors.OnAccent,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = "Ir para o mapa",
                color = RotaColors.OnAccent,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyNextStop(
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RotaColors.Surface)
            .border(1.dp, RotaColors.Line, RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = RotaColors.Accent,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhuma entrega na rota",
            color = RotaColors.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Leia o endereço do cliente para começar.",
            color = RotaColors.Secondary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(RotaColors.Accent)
                .clickable(onClick = onScanClick)
                .padding(horizontal = 18.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = RotaColors.OnAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ler endereço",
                color = RotaColors.OnAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun DeliveryKindTag(
    type: DeliveryType
) {
    val bg = if (type == DeliveryType.IFOOD) RotaColors.IFood else RotaColors.Surface2
    val fg = if (type == DeliveryType.IFOOD) Color.White else RotaColors.White
    val icon = if (type == DeliveryType.IFOOD) Icons.Default.Fastfood else Icons.Default.Inventory2

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.label,
            tint = fg,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = type.label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
