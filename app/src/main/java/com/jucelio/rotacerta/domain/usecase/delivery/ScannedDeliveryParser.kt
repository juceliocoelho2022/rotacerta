package com.jucelio.rotacerta.domain.usecase.delivery

import com.jucelio.rotacerta.domain.model.delivery.Delivery
import com.jucelio.rotacerta.domain.model.delivery.DeliveryType
import java.util.UUID
import javax.inject.Inject

/**
 * Converte o conteúdo bruto lido de um QR Code ou código de barras
 * em uma [Delivery].
 *
 * Formatos aceitos, em ordem de tentativa:
 *
 * 1. JSON simples:
 *    {"tipo":"ifood","nome":"João","endereco":"Rua A, 123",
 *     "bairro":"Centro","cidade":"Natal","cep":"59000-000",
 *     "complemento":"Apto 12","pedido":"#4821"}
 *
 * 2. Pares chave/valor, um por linha:
 *    Tipo: iFood
 *    Nome: João
 *    Endereço: Rua A, 123
 *    ...
 *
 * 3. Campos separados por barra ou ponto e vírgula:
 *    iFood|João|Rua A, 123|Centro|Natal|59000-000
 *
 * 4. Texto livre: usado integralmente como endereço.
 *
 * As chaves são reconhecidas em português e inglês, sem diferenciar
 * maiúsculas/minúsculas ou acentos.
 */
class ScannedDeliveryParser @Inject constructor() {

    fun parse(rawCode: String): Delivery {
        val raw = rawCode.trim()

        val fields = when {
            raw.startsWith("{") -> parseJson(raw)
            raw.contains('|') || raw.contains(';') -> parseDelimited(raw)
            KEY_LINE_REGEX.containsMatchIn(raw) -> parseLines(raw)
            else -> emptyMap()
        }

        val street = firstOf(fields, STREET_KEYS)
            ?: raw.takeIf { fields.isEmpty() }
            ?: raw

        return Delivery(
            id = UUID.randomUUID().toString(),
            type = DeliveryType.fromRaw(firstOf(fields, TYPE_KEYS)),
            recipientName = firstOf(fields, NAME_KEYS)
                ?.takeIf { it.isNotBlank() }
                ?: "Destinatário",
            street = street.trim(),
            neighborhood = firstOf(fields, NEIGHBORHOOD_KEYS),
            city = firstOf(fields, CITY_KEYS),
            zipCode = firstOf(fields, ZIP_KEYS)
                ?.let { normalizeZip(it) },
            complement = firstOf(fields, COMPLEMENT_KEYS),
            orderCode = firstOf(fields, ORDER_KEYS),
            rawCode = raw
        )
    }

    /**
     * Interpreta um JSON simples (sem objetos aninhados).
     * A vírgula separa campos, portanto os valores param na vírgula.
     */
    private fun parseJson(input: String): Map<String, String> {
        val body = input
            .trim()
            .removePrefix("{")
            .removeSuffix("}")

        val result = LinkedHashMap<String, String>()

        JSON_PAIR_REGEX.findAll(body).forEach { match ->
            val key = normalizeKey(match.groupValues[1].trim().lowercase())

            // Grupo 2 = valor entre aspas (vírgulas permitidas dentro);
            // Grupo 3 = valor sem aspas (para na vírgula ou chave).
            val value = match.groupValues[2]
                .ifBlank { match.groupValues[3] }
                .trim()

            if (key.isNotBlank() && value.isNotBlank()) {
                result.putIfAbsent(key, value)
            }
        }

        return result
    }

    /**
     * Interpreta pares chave/valor, um por linha, dividindo apenas
     * no primeiro separador. A vírgula é preservada porque faz parte
     * do endereço (ex.: "Rua A, 123").
     */
    private fun parseLines(input: String): Map<String, String> {
        val result = LinkedHashMap<String, String>()

        input.lines().forEach { line ->
            val separator = line.indexOfFirst { it == ':' || it == '=' }
            if (separator <= 0) return@forEach

            val key = normalizeKey(
                line.substring(0, separator).trim().lowercase()
            )
            val value = line.substring(separator + 1).trim()

            if (key.isNotBlank() && value.isNotBlank()) {
                result.putIfAbsent(key, value)
            }
        }

        return result
    }

    /**
     * Interpreta campos posicionais separados por | ou ;
     * na ordem: tipo, nome, endereço, bairro, cidade, cep.
     */
    private fun parseDelimited(input: String): Map<String, String> {
        val parts = input
            .split('|', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (parts.isEmpty()) return emptyMap()

        val result = LinkedHashMap<String, String>()
        parts.getOrNull(0)?.let { result["tipo"] = it }
        parts.getOrNull(1)?.let { result["nome"] = it }
        parts.getOrNull(2)?.let { result["endereco"] = it }
        parts.getOrNull(3)?.let { result["bairro"] = it }
        parts.getOrNull(4)?.let { result["cidade"] = it }
        parts.getOrNull(5)?.let { result["cep"] = it }

        return result
    }

    private fun firstOf(
        fields: Map<String, String>,
        keys: List<String>
    ): String? =
        keys.firstNotNullOfOrNull { key ->
            fields[key]?.takeIf { it.isNotBlank() }
        }

    private fun normalizeKey(key: String): String =
        key
            .replace('á', 'a')
            .replace('ã', 'a')
            .replace('â', 'a')
            .replace('é', 'e')
            .replace('ê', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ô', 'o')
            .replace('ç', 'c')
            .replace('ú', 'u')

    private fun normalizeZip(value: String): String {
        val digits = value.filter { it.isDigit() }
        if (digits.length != 8) return value.trim()
        return "${digits.substring(0, 5)}-${digits.substring(5)}"
    }

    private companion object {

        val TYPE_KEYS = listOf("tipo", "type", "origem", "source")
        val NAME_KEYS =
            listOf("nome", "name", "cliente", "destinatario", "recipient")
        val STREET_KEYS =
            listOf("endereco", "address", "rua", "logradouro", "street", "end")
        val NEIGHBORHOOD_KEYS = listOf("bairro", "neighborhood", "district")
        val CITY_KEYS = listOf("cidade", "city", "municipio")
        val ZIP_KEYS =
            listOf("cep", "zip", "zipcode", "postalcode", "codigopostal")
        val COMPLEMENT_KEYS =
            listOf("complemento", "complement", "referencia", "reference", "obs")
        val ORDER_KEYS =
            listOf("pedido", "order", "codigo", "code", "id", "rastreio", "tracking")

        /**
         * Par JSON "chave": valor. O valor pode vir entre aspas
         * (grupo 2, aceitando vírgulas internas) ou sem aspas
         * (grupo 3, encerrando na vírgula, chave ou fim).
         */
        val JSON_PAIR_REGEX =
            Regex("\"([\\p{L}_]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^\",}]+))")

        /** Detecta ao menos uma linha "chave: valor" com chave conhecida. */
        val KEY_LINE_REGEX =
            Regex(
                "(?im)^\\s*(tipo|type|nome|name|endereco|endereço|address|rua|" +
                    "logradouro|bairro|cidade|city|cep|zip|pedido|order|" +
                    "complemento|referencia|referência)\\s*[:=]"
            )
    }
}
