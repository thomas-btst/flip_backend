package com.flip.skateshop.service

import com.flip.skateshop.domain.Product
import com.flip.skateshop.interfaces.service.PaymentServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class PaymentServiceTest(
    @Autowired
    private val paymentService: PaymentServiceInterface,
) : ServicesCleaner() {
    @Test
    fun `should create a session correctly`() {
        val product = Product.Skate(ObjectId(), "Name", "Desc", 9L, "/path/to/picture")
        val quantity = 8L
        val items =
            listOf(
                Pair(product, quantity),
            )
        val session = paymentService.createSession(items, "test@test.com")
        assertEquals(product.price * quantity, session.amountTotal)
    }

    @Test
    fun `should retrieve a session correctly`() {
        val product = Product.Skate(ObjectId(), "Name", "Desc", 9L, "/path/to/picture")
        val quantity = 8L
        val items =
            listOf(
                Pair(product, quantity),
            )
        val session = paymentService.createSession(items, "test@test.com")
        val updatedSession = paymentService.retrieveSession(session.id)
        assertEquals(product.price * quantity, updatedSession.amountTotal)
        assertEquals(1, updatedSession.lineItems.data.size)
    }
}
