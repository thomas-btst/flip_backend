package com.flip.skateshop.service

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.Product
import com.flip.skateshop.interfaces.service.PaymentServiceInterface
import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.Price
import com.stripe.model.checkout.Session
import com.stripe.net.RequestOptions
import com.stripe.param.PriceCreateParams
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionRetrieveParams
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val properties: SkateshopProperties,
) : PaymentServiceInterface {
    @PostConstruct
    fun init() {
        Stripe.apiKey = properties.stripe.privateKey
    }

    @Throws(StripeException::class)
    override fun createSession(
        items: List<Pair<Product, Long>>,
        email: String,
    ): Session {
        val lines =
            items.map { (product, quantity) ->
                val price =
                    Price.create(
                        PriceCreateParams
                            .builder()
                            .setCurrency("eur")
                            .setUnitAmount(product.price)
                            .setProductData(
                                PriceCreateParams.ProductData
                                    .builder()
                                    .setName(product.name)
                                    .build(),
                            ).build(),
                    )
                SessionCreateParams.LineItem
                    .builder()
                    .setQuantity(quantity)
                    .setPrice(price.id)
                    .build()
            }
        val params =
            SessionCreateParams
                .builder()
                .apply {
                    setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                    setMode(SessionCreateParams.Mode.PAYMENT)
                    setReturnUrl("${properties.client.url}/payment/return?session_id={CHECKOUT_SESSION_ID}")
                    setCustomerEmail(email)
                    lines.forEach { addLineItem(it) }
                }.build()
        return Session.create(params)
    }

    override fun retrieveSession(sessionId: String): Session {
        val retrieveParams: SessionRetrieveParams =
            SessionRetrieveParams
                .builder()
                .addExpand("line_items.data.price.product")
                .build()
        return Session.retrieve(sessionId, retrieveParams, RequestOptions.builder().build())
//            if (session.lineItems != null) {
//                session.lineItems.data.forEach { lineItem ->
//                    System.out.println("Line item: " + lineItem.description)
//                    System.out.println("Quantity: " + lineItem.quantity)
//                    System.out.println("Price: " + lineItem.amountTotal)
//                }
//            }
    }
}
