package com.flip.skateshop.web.rest

import com.flip.skateshop.interfaces.service.CartServiceInterface
import com.flip.skateshop.web.rest.dto.CartDto
import com.flip.skateshop.web.rest.dto.CartQuantityDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/carts")
class CartController(
    private val cartService: CartServiceInterface,
) {
    @PatchMapping("/{id}")
    @Operation(summary = "Add a product with a quantity to cart")
    @ApiResponses(
        ApiResponse(responseCode = "204"),
        ApiResponse(responseCode = "404", description = "Product not found"),
    )
    suspend fun addProductToCart(
        @PathVariable id: ObjectId,
        @RequestBody @Valid cart: CartQuantityDto,
    ) {
        cartService.addProductToCart(id, cart.quantity)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product from cart")
    @ApiResponses(
        ApiResponse(responseCode = "204"),
        ApiResponse(responseCode = "404", description = "Product not found in cart"),
    )
    suspend fun deleteProductFromCart(
        @PathVariable id: ObjectId,
    ) {
        cartService.removeProductFromCart(id)
    }

    @GetMapping
    @Operation(summary = "Get cart")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getCart(): CartDto = cartService.getCart()

    @GetMapping("/{id}/quantity")
    @Operation(summary = "Get the quantity of a product in the cart")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getCartQuantity(
        @PathVariable id: ObjectId,
    ): CartQuantityDto = cartService.getCartQuantity(id)
}
