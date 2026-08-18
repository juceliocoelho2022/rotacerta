package com.jucelio.rotacerta.domain.usecase.delivery

import com.jucelio.rotacerta.domain.model.delivery.DeliveryType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ScannedDeliveryParserTest {

    private lateinit var parser: ScannedDeliveryParser

    @Before
    fun setup() {
        parser = ScannedDeliveryParser()
    }

    @Test
    fun `deve interpretar JSON de entrega iFood`() {
        val raw = """
            {"tipo":"ifood","nome":"João Silva",
             "endereco":"Rua das Flores, 123","bairro":"Centro",
             "cidade":"Natal","cep":"59000-000",
             "complemento":"Apto 12","pedido":"#4821"}
        """.trimIndent()

        val delivery = parser.parse(raw)

        assertEquals(DeliveryType.IFOOD, delivery.type)
        assertEquals("João Silva", delivery.recipientName)
        assertEquals("Rua das Flores, 123", delivery.street)
        assertEquals("Centro", delivery.neighborhood)
        assertEquals("Natal", delivery.city)
        assertEquals("59000-000", delivery.zipCode)
        assertEquals("Apto 12", delivery.complement)
        assertEquals("#4821", delivery.orderCode)
    }

    @Test
    fun `deve preservar virgula do endereco no formato por linhas`() {
        val raw = """
            Tipo: Encomenda
            Nome: Maria Souza
            Endereço: Av. Brasil, 4500
            Bairro: Lagoa Nova
            Cidade: Natal
            CEP: 59064-100
        """.trimIndent()

        val delivery = parser.parse(raw)

        assertEquals(DeliveryType.PACKAGE, delivery.type)
        assertEquals("Maria Souza", delivery.recipientName)
        assertEquals("Av. Brasil, 4500", delivery.street)
        assertEquals("Lagoa Nova", delivery.neighborhood)
        assertEquals("59064-100", delivery.zipCode)
    }

    @Test
    fun `deve interpretar campos separados por barra`() {
        val raw = "iFood|Carlos|Rua A, 10|Tirol|Natal|59015-000"

        val delivery = parser.parse(raw)

        assertEquals(DeliveryType.IFOOD, delivery.type)
        assertEquals("Carlos", delivery.recipientName)
        assertEquals("Rua A, 10", delivery.street)
        assertEquals("Tirol", delivery.neighborhood)
        assertEquals("59015-000", delivery.zipCode)
    }

    @Test
    fun `deve normalizar CEP com oito digitos sem hifen`() {
        val raw = "Nome: Ana\nEndereço: Rua X, 1\nCEP: 59000000"

        val delivery = parser.parse(raw)

        assertEquals("59000-000", delivery.zipCode)
    }

    @Test
    fun `deve tratar texto livre como endereco`() {
        val raw = "Rua Sem Estrutura, 999 - Ponta Negra"

        val delivery = parser.parse(raw)

        assertEquals("Rua Sem Estrutura, 999 - Ponta Negra", delivery.street)
        assertEquals("Destinatário", delivery.recipientName)
        // Sem indicação de comida, o padrão é encomenda.
        assertEquals(DeliveryType.PACKAGE, delivery.type)
    }

    @Test
    fun `deve preservar o codigo bruto original`() {
        val raw = "  Rua Y, 2  "

        val delivery = parser.parse(raw)

        assertEquals("Rua Y, 2", delivery.rawCode)
    }
}
