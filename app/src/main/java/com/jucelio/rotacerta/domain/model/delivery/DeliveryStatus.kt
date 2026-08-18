package com.jucelio.rotacerta.domain.model.delivery

/**
 * Situação de uma parada dentro da rota de entrega.
 */
enum class DeliveryStatus(
    val label: String
) {
    PENDING("Pendente"),
    DELIVERED("Entregue"),
    FAILED("Não entregue")
}
