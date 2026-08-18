package com.jucelio.rotacerta.ui.delivery

import androidx.compose.foundation.background
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jucelio.rotacerta.domain.model.delivery.Delivery
import com.jucelio.rotacerta.domain.model.delivery.DeliveryStatus
import com.jucelio.rotacerta.domain.model.delivery.DeliveryType

// Paleta RotaCerta (grafite neutro + verde de acento).
private val Background = RotaColors.Background
private val Surface = RotaColors.Surface
private val Accent = RotaColors.Accent
private val Green = RotaColors.Accent
private val Red = RotaColors.Danger
private val IFoodRed = RotaColors.IFood
private val WhiteText = RotaColors.White
private val SecondaryText = RotaColors.Secondary
private val OnAccent = RotaColors.OnAccent

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DeliveryRouteScreen(
    state: DeliveryUiState,
    onBack: () -> Unit,
    onScanClick: () -> Unit,
    onReorder: () -> Unit,
    onClear: () -> Unit,
    onMarkDelivered: (String) -> Unit,
    onMarkFailed: (String) -> Unit,
    onReopen: (String) -> Unit,
    onRemove: (String) -> Unit,
    onMessageShown: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.infoMessage) {
        val message = state.infoMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RotaCerta",
                        color = WhiteText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = WhiteText
                        )
                    }
                },
                actions = {
                    if (state.deliveries.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Limpar rota",
                                tint = SecondaryText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onScanClick,
                containerColor = Accent,
                contentColor = OnAccent,
                icon = {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null
                    )
                },
                text = { Text("Ler endereço") }
            )
        }
    ) { innerPadding ->

        if (state.deliveries.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onScanClick = onScanClick
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
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RouteSummaryCard(
                    state = state,
                    onReorder = onReorder
                )
            }

            itemsIndexed(
                items = state.deliveries,
                key = { _, delivery -> delivery.id }
            ) { index, delivery ->
                DeliveryStopCard(
                    position = index + 1,
                    delivery = delivery,
                    isNextStop = delivery.id == state.nextStop?.id,
                    onOpenMap = {
                        openInMaps(context, delivery)
                    },
                    onMarkDelivered = { onMarkDelivered(delivery.id) },
                    onMarkFailed = { onMarkFailed(delivery.id) },
                    onReopen = { onReopen(delivery.id) },
                    onRemove = { onRemove(delivery.id) }
                )
            }
        }
    }
}

@Composable
private fun RouteSummaryCard(
    state: DeliveryUiState,
    onReorder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    value = state.totalStops.toString(),
                    label = "Paradas",
                    color = WhiteText
                )
                SummaryItem(
                    value = state.pendingStops.toString(),
                    label = "Pendentes",
                    color = Accent
                )
                SummaryItem(
                    value = state.deliveredStops.toString(),
                    label = "Entregues",
                    color = Green
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onReorder,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Accent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Recalcular rota")
            }
        }
    }
}

@Composable
private fun RowScope.SummaryItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = SecondaryText,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DeliveryStopCard(
    position: Int,
    delivery: Delivery,
    isNextStop: Boolean,
    onOpenMap: () -> Unit,
    onMarkDelivered: () -> Unit,
    onMarkFailed: () -> Unit,
    onReopen: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = if (isNextStop) {
            BorderStroke(width = 1.5.dp, color = Accent)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                PositionBadge(
                    position = position,
                    concluded = delivery.isConcluded
                )

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (delivery.type == DeliveryType.IFOOD) {
                                Icons.Default.Fastfood
                            } else {
                                Icons.Default.Inventory2
                            },
                            contentDescription = delivery.type.label,
                            tint = if (delivery.type == DeliveryType.IFOOD) {
                                IFoodRed
                            } else {
                                Accent
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = delivery.type.label,
                            color = SecondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isNextStop) {
                            Spacer(modifier = Modifier.size(8.dp))
                            NextStopTag()
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = delivery.recipientName,
                        color = WhiteText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remover parada",
                        tint = SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = delivery.fullAddress,
                color = SecondaryText,
                fontSize = 14.sp,
                lineHeight = 19.sp
            )

            if (!delivery.complement.isNullOrBlank()) {
                Text(
                    text = "Ref.: ${delivery.complement}",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }

            if (!delivery.orderCode.isNullOrBlank()) {
                Text(
                    text = "Pedido: ${delivery.orderCode}",
                    color = SecondaryText,
                    fontSize = 13.sp
                )
            }

            StatusLabel(status = delivery.status)

            Spacer(modifier = Modifier.height(12.dp))

            StopActions(
                delivery = delivery,
                onOpenMap = onOpenMap,
                onMarkDelivered = onMarkDelivered,
                onMarkFailed = onMarkFailed,
                onReopen = onReopen
            )
        }
    }
}

@Composable
private fun StopActions(
    delivery: Delivery,
    onOpenMap: () -> Unit,
    onMarkDelivered: () -> Unit,
    onMarkFailed: () -> Unit,
    onReopen: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onOpenMap,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WhiteText
            )
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("Mapa", fontSize = 13.sp)
        }

        if (delivery.isConcluded) {
            OutlinedButton(
                onClick = onReopen,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Accent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Reabrir", fontSize = 13.sp)
            }
        } else {
            OutlinedButton(
                onClick = onMarkFailed,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Red
                )
            ) {
                Text("Falhou", fontSize = 13.sp)
            }

            Button(
                onClick = onMarkDelivered,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = OnAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Entregue", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PositionBadge(
    position: Int,
    concluded: Boolean
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                color = if (concluded) {
                    Green.copy(alpha = 0.18f)
                } else {
                    Accent.copy(alpha = 0.20f)
                },
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (concluded) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = position.toString(),
                color = Accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NextStopTag() {
    Box(
        modifier = Modifier
            .background(
                color = Accent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "PRÓXIMA",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusLabel(status: DeliveryStatus) {
    val (color, text) = when (status) {
        DeliveryStatus.PENDING -> SecondaryText to "Pendente"
        DeliveryStatus.DELIVERED -> Green to "Entregue"
        DeliveryStatus.FAILED -> Red to "Não entregue"
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onScanClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Route,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Nenhuma entrega na rota",
            color = WhiteText,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Leia o endereço do cliente por QR Code ou código de " +
                "barras para montar a rota de entrega, uma parada após a outra.",
            color = SecondaryText,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = OnAccent
            )
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Ler primeiro endereço")
        }
    }
}
