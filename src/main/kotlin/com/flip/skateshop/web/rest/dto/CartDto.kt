package com.flip.skateshop.web.rest.dto

import jakarta.validation.constraints.PositiveOrZero

class CartDto(
    val products: List<CartItemDto>,
)

class CartItemDto(
    val id: String,
    val name: String,
    val price: Long,
    val picture: String,
    val quantity: Long,
)

class CartQuantityDto(
    @field:PositiveOrZero
    val quantity: Long,
)
