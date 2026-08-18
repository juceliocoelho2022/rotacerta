package com.jucelio.rotacerta.domain.model.delivery

/**
 * Origem da entrega.
 *
 * O sistema atende tanto pedidos de aplicativos de comida
 * (por exemplo iFood) quanto encomendas de transportadoras.
 */
enum class DeliveryType(
    val label: String
) {
    IFOOD("iFood"),
    PACKAGE("Encomenda");

    companion object {

        /**
         * Identifica o tipo a partir de um texto livre lido do código.
         *
         * Aceita valores como "ifood", "food", "comida",
         * "encomenda", "package", "correios" etc.
         */
        fun fromRaw(raw: String?): DeliveryType {
            val normalized = raw
                ?.trim()
                ?.lowercase()
                .orEmpty()

            return when {
                normalized.contains("ifood") -> IFOOD
                normalized.contains("food") -> IFOOD
                normalized.contains("comida") -> IFOOD
                normalized.contains("restaurante") -> IFOOD
                else -> PACKAGE
            }
        }
    }
}
