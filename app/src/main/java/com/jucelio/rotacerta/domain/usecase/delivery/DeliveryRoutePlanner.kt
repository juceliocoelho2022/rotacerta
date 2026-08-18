package com.jucelio.rotacerta.domain.usecase.delivery

import com.jucelio.rotacerta.domain.model.delivery.Delivery
import javax.inject.Inject

/**
 * Monta a rota de entrega ordenando as paradas "uma após a outra".
 *
 * Estratégia (sem geocodificação disponível no dispositivo):
 *
 * - Entregas pendentes vêm primeiro, agrupadas por região
 *   (CEP / cidade / bairro) e, dentro do grupo, por logradouro.
 *   Assim endereços próximos ficam em sequência, reduzindo o
 *   deslocamento do entregador.
 * - Entregas já concluídas (entregues ou não entregues) vão para o
 *   fim da lista, preservando a ordem em que foram finalizadas.
 *
 * A ordenação é estável: entregas equivalentes mantêm a ordem de
 * inserção, tornando o resultado previsível e testável.
 */
class DeliveryRoutePlanner @Inject constructor() {

    fun plan(deliveries: List<Delivery>): List<Delivery> {
        val (pending, concluded) =
            deliveries.partition { !it.isConcluded }

        val orderedPending = pending.sortedWith(
            compareBy(
                { it.clusterKey },
                { it.street.trim().lowercase() }
            )
        )

        return orderedPending + concluded
    }

    /**
     * Próxima parada da rota: a primeira entrega ainda pendente,
     * já respeitando a ordenação da rota.
     */
    fun nextStop(deliveries: List<Delivery>): Delivery? =
        plan(deliveries).firstOrNull { !it.isConcluded }
}
