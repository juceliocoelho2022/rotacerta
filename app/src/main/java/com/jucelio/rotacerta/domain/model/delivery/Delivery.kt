package com.jucelio.rotacerta.domain.model.delivery

/**
 * Modelo de domínio de uma entrega.
 *
 * Representa uma parada da rota do entregador, montada a partir do
 * endereço lido por QR Code ou código de barras. Não possui
 * dependência de Retrofit, Gson ou Android.
 */
data class Delivery(
    val id: String,
    val type: DeliveryType,
    val recipientName: String,
    val street: String,
    val neighborhood: String? = null,
    val city: String? = null,
    val zipCode: String? = null,
    val complement: String? = null,
    val orderCode: String? = null,
    val rawCode: String,
    val status: DeliveryStatus = DeliveryStatus.PENDING
) {

    /**
     * Endereço completo, pronto para exibição ou para abertura
     * em um aplicativo de mapas.
     */
    val fullAddress: String
        get() = buildList {
            if (street.isNotBlank()) add(street)
            if (!neighborhood.isNullOrBlank()) add(neighborhood)
            if (!city.isNullOrBlank()) add(city)
            if (!zipCode.isNullOrBlank()) add("CEP $zipCode")
        }.joinToString(separator = ", ")

    /**
     * Chave usada para agrupar entregas próximas ao montar a rota.
     *
     * Prioriza o CEP; na ausência dele, cai para bairro/cidade.
     * Assim, endereços da mesma região tendem a ficar juntos.
     */
    val clusterKey: String
        get() = listOfNotNull(
            zipCode?.filter { it.isDigit() }?.take(5)?.ifBlank { null },
            city?.trim()?.lowercase()?.ifBlank { null },
            neighborhood?.trim()?.lowercase()?.ifBlank { null }
        ).joinToString(separator = "|")

    val isConcluded: Boolean
        get() = status != DeliveryStatus.PENDING
}
