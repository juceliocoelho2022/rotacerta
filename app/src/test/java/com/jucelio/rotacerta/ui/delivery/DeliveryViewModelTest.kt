package com.jucelio.rotacerta.ui.delivery

import com.jucelio.rotacerta.domain.model.delivery.DeliveryStatus
import com.jucelio.rotacerta.domain.usecase.delivery.DeliveryRoutePlanner
import com.jucelio.rotacerta.domain.usecase.delivery.ScannedDeliveryParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeliveryViewModelTest {

    private lateinit var viewModel: DeliveryViewModel

    @Before
    fun setup() {
        viewModel = DeliveryViewModel(
            parser = ScannedDeliveryParser(),
            routePlanner = DeliveryRoutePlanner()
        )
    }

    @Test
    fun `deve adicionar parada ao ler codigo`() {
        viewModel.addFromScannedCode(
            "Nome: João\nEndereço: Rua A, 1\nCEP: 59000-000"
        )

        assertEquals(1, viewModel.state.totalStops)
        assertEquals(1, viewModel.state.pendingStops)
        assertEquals("João", viewModel.state.deliveries.first().recipientName)
    }

    @Test
    fun `nao deve adicionar codigo duplicado`() {
        val code = "Endereço: Rua A, 1\nCEP: 59000-000"

        viewModel.addFromScannedCode(code)
        viewModel.addFromScannedCode(code)

        assertEquals(1, viewModel.state.totalStops)
        assertEquals("Esta entrega já está na rota.", viewModel.state.infoMessage)
    }

    @Test
    fun `nao deve adicionar codigo em branco`() {
        viewModel.addFromScannedCode("   ")

        assertEquals(0, viewModel.state.totalStops)
        assertNotNull(viewModel.state.infoMessage)
    }

    @Test
    fun `deve reordenar a rota agrupando por CEP ao adicionar`() {
        viewModel.addFromScannedCode("Endereço: Rua 1\nCEP: 59100-000")
        viewModel.addFromScannedCode("Endereço: Rua 2\nCEP: 59000-000")
        viewModel.addFromScannedCode("Endereço: Rua 3\nCEP: 59100-000")

        val ceps = viewModel.state.deliveries.map { it.zipCode }

        // CEPs iguais ficam adjacentes após o planejamento da rota.
        assertEquals(
            listOf("59000-000", "59100-000", "59100-000"),
            ceps
        )
    }

    @Test
    fun `deve marcar como entregue e mover para o fim da rota`() {
        viewModel.addFromScannedCode("Endereço: Rua A\nCEP: 59000-000")
        viewModel.addFromScannedCode("Endereço: Rua B\nCEP: 59000-000")

        val firstId = viewModel.state.deliveries.first().id
        viewModel.markAsDelivered(firstId)

        val last = viewModel.state.deliveries.last()
        assertEquals(firstId, last.id)
        assertEquals(DeliveryStatus.DELIVERED, last.status)
        assertEquals(1, viewModel.state.deliveredStops)
        assertEquals(1, viewModel.state.pendingStops)
    }

    @Test
    fun `proxima parada deve pular entregas concluidas`() {
        viewModel.addFromScannedCode("Endereço: Rua A\nCEP: 59000-000")
        viewModel.addFromScannedCode("Endereço: Rua B\nCEP: 59000-000")

        val firstId = viewModel.state.nextStop!!.id
        viewModel.markAsDelivered(firstId)

        val next = viewModel.state.nextStop
        assertNotNull(next)
        assertTrue(next!!.id != firstId)
        assertEquals(DeliveryStatus.PENDING, next.status)
    }

    @Test
    fun `deve remover uma parada`() {
        viewModel.addFromScannedCode("Endereço: Rua A\nCEP: 59000-000")
        val id = viewModel.state.deliveries.first().id

        viewModel.removeStop(id)

        assertEquals(0, viewModel.state.totalStops)
    }

    @Test
    fun `deve limpar a rota inteira`() {
        viewModel.addFromScannedCode("Endereço: Rua A")
        viewModel.addFromScannedCode("Endereço: Rua B")

        viewModel.clearRoute()

        assertEquals(0, viewModel.state.totalStops)
        assertNull(viewModel.state.infoMessage)
    }

    @Test
    fun `deve alternar disponibilidade online`() {
        assertTrue(viewModel.state.isOnline)

        viewModel.toggleOnline()
        assertEquals(false, viewModel.state.isOnline)

        viewModel.toggleOnline()
        assertTrue(viewModel.state.isOnline)
    }

    @Test
    fun `historico deve conter apenas entregas concluidas`() {
        viewModel.addFromScannedCode("Endereço: Rua A\nCEP: 59000-000")
        viewModel.addFromScannedCode("Endereço: Rua B\nCEP: 59000-000")

        val firstId = viewModel.state.deliveries.first().id
        viewModel.markAsDelivered(firstId)

        assertEquals(1, viewModel.state.history.size)
        assertEquals(firstId, viewModel.state.history.first().id)
    }

    @Test
    fun `progresso deve refletir paradas concluidas`() {
        viewModel.addFromScannedCode("Endereço: Rua A\nCEP: 59000-000")
        viewModel.addFromScannedCode("Endereço: Rua B\nCEP: 59000-000")

        assertEquals(0f, viewModel.state.progress)

        val firstId = viewModel.state.nextStop!!.id
        viewModel.markAsDelivered(firstId)

        assertEquals(0.5f, viewModel.state.progress)
    }
}
