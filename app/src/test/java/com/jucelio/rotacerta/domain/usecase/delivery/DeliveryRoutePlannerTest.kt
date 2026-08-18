package com.jucelio.rotacerta.domain.usecase.delivery

import com.jucelio.rotacerta.domain.model.delivery.Delivery
import com.jucelio.rotacerta.domain.model.delivery.DeliveryStatus
import com.jucelio.rotacerta.domain.model.delivery.DeliveryType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeliveryRoutePlannerTest {

    private lateinit var planner: DeliveryRoutePlanner

    @Before
    fun setup() {
        planner = DeliveryRoutePlanner()
    }

    private fun delivery(
        id: String,
        street: String,
        zip: String? = null,
        neighborhood: String? = null,
        status: DeliveryStatus = DeliveryStatus.PENDING
    ) = Delivery(
        id = id,
        type = DeliveryType.PACKAGE,
        recipientName = id,
        street = street,
        neighborhood = neighborhood,
        zipCode = zip,
        rawCode = id,
        status = status
    )

    @Test
    fun `deve agrupar entregas do mesmo CEP em sequencia`() {
        val deliveries = listOf(
            delivery("A", "Rua 1", zip = "59000-000"),
            delivery("B", "Rua 2", zip = "59100-000"),
            delivery("C", "Rua 3", zip = "59000-000")
        )

        val route = planner.plan(deliveries)

        // A e C (mesmo CEP 59000) ficam juntas, antes de B (59100).
        assertEquals(listOf("A", "C", "B"), route.map { it.id })
    }

    @Test
    fun `deve ordenar por logradouro dentro do mesmo grupo`() {
        val deliveries = listOf(
            delivery("A", "Rua Zebra", zip = "59000-000"),
            delivery("B", "Rua Abelha", zip = "59000-000")
        )

        val route = planner.plan(deliveries)

        assertEquals(listOf("B", "A"), route.map { it.id })
    }

    @Test
    fun `deve empurrar entregas concluidas para o fim`() {
        val deliveries = listOf(
            delivery("A", "Rua 1", zip = "59000-000",
                status = DeliveryStatus.DELIVERED),
            delivery("B", "Rua 2", zip = "59100-000"),
            delivery("C", "Rua 3", zip = "59050-000")
        )

        val route = planner.plan(deliveries)

        // Pendentes primeiro (ordenadas), concluída no fim.
        assertEquals(listOf("C", "B", "A"), route.map { it.id })
    }

    @Test
    fun `proxima parada deve ser a primeira pendente da rota`() {
        val deliveries = listOf(
            delivery("A", "Rua Zebra", zip = "59000-000",
                status = DeliveryStatus.DELIVERED),
            delivery("B", "Rua Abelha", zip = "59000-000"),
            delivery("C", "Rua Gato", zip = "59000-000")
        )

        val next = planner.nextStop(deliveries)

        assertEquals("B", next?.id)
    }

    @Test
    fun `proxima parada deve ser nula quando tudo foi concluido`() {
        val deliveries = listOf(
            delivery("A", "Rua 1", status = DeliveryStatus.DELIVERED),
            delivery("B", "Rua 2", status = DeliveryStatus.FAILED)
        )

        assertEquals(null, planner.nextStop(deliveries))
    }
}
