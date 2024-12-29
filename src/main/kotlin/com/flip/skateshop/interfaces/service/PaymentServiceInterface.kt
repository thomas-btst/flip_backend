package com.flip.skateshop.interfaces.service

import com.flip.skateshop.domain.Product
import com.stripe.model.checkout.Session

interface PaymentServiceInterface {
    fun createSession(
        items: List<Pair<Product, Long>>,
        email: String,
    ): Session

    fun retrieveSession(sessionId: String): Session
}
