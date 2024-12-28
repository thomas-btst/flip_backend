package com.flip.skateshop.service

import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.CartServiceInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.CartMapper
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.web.rest.dto.CartDto
import com.flip.skateshop.web.rest.dto.CartQuantityDto
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CartService(
    private val userService: UserServiceInterface,
    private val securityUtils: SecurityUtils,
    private val userRepository: UserRepositoryInterface,
    private val productRepository: ProductRepositoryInterface,
    private val cartMapper: CartMapper,
) : CartServiceInterface {
    override suspend fun getCart(): CartDto {
        val user = userService.getCurrentUser()
        val productIds = user.cart.map { it.key }
        val products = productRepository.findByIdIn(productIds)
        return cartMapper.toCartDto(user.cart, products.toList())
    }

    override suspend fun addProduct(
        productId: ObjectId,
        quantity: Long,
    ) {
        productRepository.findById(productId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product with id $productId does not exist")
        if (quantity < 1) {
            userRepository.removeFromCart(securityUtils.getCurrentUserId(), productId)
        } else {
            userRepository.addToCart(securityUtils.getCurrentUserId(), productId, quantity)
        }
    }

    override suspend fun removeProduct(productId: ObjectId) {
        val result = userRepository.removeFromCart(securityUtils.getCurrentUserId(), productId)
        if (result.modifiedCount < 1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart")
        }
    }

    override suspend fun getCartQuantity(productId: ObjectId): CartQuantityDto {
        val user = userService.getCurrentUser()
        return CartQuantityDto(user.cart.get(productId) ?: 0)
    }

    override suspend fun clearCart() {
        val userId = securityUtils.getCurrentUserId()
        userRepository.clearCart(userId)
    }
}
