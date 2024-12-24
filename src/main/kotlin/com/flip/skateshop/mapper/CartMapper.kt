package com.flip.skateshop.mapper

import com.flip.skateshop.domain.Product
import com.flip.skateshop.web.rest.dto.CartDto
import com.flip.skateshop.web.rest.dto.CartItemDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class CartMapper(
    private val fileMapper: FileMapper,
) {
    fun toCartDto(
        cart: Map<ObjectId, Long>,
        products: List<Product>,
    ): CartDto =
        CartDto(
            cart.flatMap { (id, quantity) ->
                val product = products.firstOrNull { it._id == id }
                if (product == null) {
                    emptyList()
                } else {
                    listOf(
                        CartItemDto(
                            id.toHexString(),
                            product.name,
                            product.price,
                            fileMapper.toPublicPath(product.picture),
                            quantity,
                        ),
                    )
                }
            },
        )
}
