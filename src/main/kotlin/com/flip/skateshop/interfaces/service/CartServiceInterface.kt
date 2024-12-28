package com.flip.skateshop.interfaces.service

import com.flip.skateshop.web.rest.dto.CartDto
import com.flip.skateshop.web.rest.dto.CartQuantityDto
import org.bson.types.ObjectId

interface CartServiceInterface {
    suspend fun getCart(): CartDto

    suspend fun addProduct(
        productId: ObjectId,
        quantity: Long,
    )

    suspend fun removeProduct(productId: ObjectId)

    suspend fun getCartQuantity(productId: ObjectId): CartQuantityDto

    suspend fun clearCart()
}
