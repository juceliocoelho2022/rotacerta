package com.jucelio.rotacerta.ui.delivery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jucelio.rotacerta.domain.model.delivery.Delivery
import com.jucelio.rotacerta.domain.model.delivery.DeliveryStatus
import com.jucelio.rotacerta.domain.usecase.delivery.DeliveryRoutePlanner
import com.jucelio.rotacerta.domain.usecase.delivery.ScannedDeliveryParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class DeliveryUiState(
    val deliveries: List<Delivery> = emptyList(),
    val isOnline: Boolean = true,
    val infoMessage: String? = null
) {
    val totalStops: Int
        get() = deliveries.size

    val pendingStops: Int
        get() = deliveries.count { !it.isConcluded }

    val deliveredStops: Int
        get() = deliveries.count { it.status == DeliveryStatus.DELIVERED }

    val failedStops: Int
        get() = deliveries.count { it.status == DeliveryStatus.FAILED }

    val nextStop: Delivery?
        get() = deliveries.firstOrNull { !it.isConcluded }

    /** Entregas já finalizadas (entregues ou não), para o histórico. */
    val history: List<Delivery>
        get() = deliveries.filter { it.isConcluded }

    /** Progresso da rota (0f..1f) considerando as paradas concluídas. */
    val progress: Float
        get() = if (totalStops == 0) {
            0f
        } else {
            (totalStops - pendingStops).toFloat() / totalStops
        }
}

/**
 * Gerencia a rota de entregas do entregador.
 *
 * As paradas são lidas por QR Code ou código de barras, convertidas
 * em [Delivery] pelo [ScannedDeliveryParser] e ordenadas em rota pelo
 * [DeliveryRoutePlanner]. O estado é mantido em memória durante a
 * sessão de entregas.
 */
@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val parser: ScannedDeliveryParser,
    private val routePlanner: DeliveryRoutePlanner
) : ViewModel() {

    var state by mutableStateOf(DeliveryUiState())
        private set

    /**
     * Adiciona uma entrega a partir do conteúdo lido do código e já
     * reordena a rota. Ignora códigos em branco e duplicados.
     */
    fun addFromScannedCode(rawCode: String) {
        val normalized = rawCode.trim()

        if (normalized.isBlank()) {
            state = state.copy(
                infoMessage = "Código vazio. Tente novamente."
            )
            return
        }

        val alreadyAdded = state.deliveries.any {
            it.rawCode.equals(normalized, ignoreCase = true)
        }

        if (alreadyAdded) {
            state = state.copy(
                infoMessage = "Esta entrega já está na rota."
            )
            return
        }

        val delivery = parser.parse(normalized)

        val updated = routePlanner.plan(
            state.deliveries + delivery
        )

        state = state.copy(
            deliveries = updated,
            infoMessage = "Parada adicionada: ${delivery.recipientName}"
        )
    }

    /** Marca uma parada como entregue e reordena a rota. */
    fun markAsDelivered(id: String) {
        updateStatus(id, DeliveryStatus.DELIVERED)
    }

    /** Marca uma parada como não entregue e reordena a rota. */
    fun markAsFailed(id: String) {
        updateStatus(id, DeliveryStatus.FAILED)
    }

    /** Retorna uma parada concluída para o estado pendente. */
    fun reopen(id: String) {
        updateStatus(id, DeliveryStatus.PENDING)
    }

    fun removeStop(id: String) {
        state = state.copy(
            deliveries = state.deliveries.filterNot { it.id == id },
            infoMessage = null
        )
    }

    /** Recalcula a ordem da rota mantendo os status atuais. */
    fun reorderRoute() {
        state = state.copy(
            deliveries = routePlanner.plan(state.deliveries),
            infoMessage = "Rota recalculada."
        )
    }

    fun clearRoute() {
        state = state.copy(
            deliveries = emptyList(),
            infoMessage = null
        )
    }

    /** Alterna a disponibilidade do entregador (online/offline). */
    fun toggleOnline() {
        state = state.copy(isOnline = !state.isOnline)
    }

    fun consumeMessage() {
        if (state.infoMessage != null) {
            state = state.copy(infoMessage = null)
        }
    }

    private fun updateStatus(id: String, status: DeliveryStatus) {
        val updated = state.deliveries.map { delivery ->
            if (delivery.id == id) {
                delivery.copy(status = status)
            } else {
                delivery
            }
        }

        state = state.copy(
            deliveries = routePlanner.plan(updated),
            infoMessage = null
        )
    }
}
