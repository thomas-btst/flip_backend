package com.flip.skateshop.web.rest.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import jakarta.validation.constraints.NotBlank

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = CreateProductDto.Skate::class, name = Product.Skate.CLASS_NAME),
    JsonSubTypes.Type(value = CreateProductDto.Deck::class, name = Product.Deck.CLASS_NAME),
    JsonSubTypes.Type(value = CreateProductDto.Wheel::class, name = Product.Wheel.CLASS_NAME),
    JsonSubTypes.Type(value = CreateProductDto.Bearing::class, name = Product.Bearing.CLASS_NAME),
    JsonSubTypes.Type(value = CreateProductDto.GridTape::class, name = Product.GridTape.CLASS_NAME),
    JsonSubTypes.Type(value = CreateProductDto.Truck::class, name = Product.Truck.CLASS_NAME),
)
sealed class CreateProductDto(
    @NotBlank
    val name: String,
    val description: String,
    val price: Long,
) {
    class Skate(
        name: String,
        description: String,
        price: Long,
    ) : CreateProductDto(name, description, price)

    class Deck(
        name: String,
        description: String,
        price: Long,
    ) : CreateProductDto(name, description, price)

    class Wheel(
        name: String,
        description: String,
        price: Long,
    ) : CreateProductDto(name, description, price)

    class Bearing(
        name: String,
        description: String,
        price: Long,
    ) : CreateProductDto(name, description, price)

    class GridTape(
        name: String,
        description: String,
        price: Long,
    ) : CreateProductDto(name, description, price)

    class Truck(
        name: String,
        description: String,
        price: Long,
    ) : CreateProductDto(name, description, price)
}

sealed class ProductDto(
    @Suppress("unused")
    val id: String,
    val name: String,
    @Suppress("unused")
    val description: String,
    @Suppress("unused")
    val price: Long,
    @Suppress("unused")
    val picture: String,
    val rate: Double?,
) {
    @Suppress("unused")
    val type: ProductType
        get() =
            when (this) {
                is Skate -> ProductType.SKATE
                is Deck -> ProductType.DECK
                is Wheel -> ProductType.WHEEL
                is Bearing -> ProductType.BEARING
                is GridTape -> ProductType.GRID_TAPE
                is Truck -> ProductType.TRUCK
            }

    class Skate(
        id: String,
        name: String,
        description: String,
        price: Long,
        picture: String,
        rate: Double?,
    ) : ProductDto(id, name, description, price, picture, rate)

    class Deck(
        id: String,
        name: String,
        description: String,
        price: Long,
        picture: String,
        rate: Double?,
    ) : ProductDto(id, name, description, price, picture, rate)

    class Wheel(
        id: String,
        name: String,
        description: String,
        price: Long,
        picture: String,
        rate: Double?,
    ) : ProductDto(id, name, description, price, picture, rate)

    class Bearing(
        id: String,
        name: String,
        description: String,
        price: Long,
        picture: String,
        rate: Double?,
    ) : ProductDto(id, name, description, price, picture, rate)

    class GridTape(
        id: String,
        name: String,
        description: String,
        price: Long,
        picture: String,
        rate: Double?,
    ) : ProductDto(id, name, description, price, picture, rate)

    class Truck(
        id: String,
        name: String,
        description: String,
        price: Long,
        picture: String,
        rate: Double?,
    ) : ProductDto(id, name, description, price, picture, rate)
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = UpdateProductDto.Skate::class, name = Product.Skate.CLASS_NAME),
    JsonSubTypes.Type(value = UpdateProductDto.Deck::class, name = Product.Deck.CLASS_NAME),
    JsonSubTypes.Type(value = UpdateProductDto.Wheel::class, name = Product.Wheel.CLASS_NAME),
    JsonSubTypes.Type(value = UpdateProductDto.Bearing::class, name = Product.Bearing.CLASS_NAME),
    JsonSubTypes.Type(value = UpdateProductDto.GridTape::class, name = Product.GridTape.CLASS_NAME),
    JsonSubTypes.Type(value = UpdateProductDto.Truck::class, name = Product.Truck.CLASS_NAME),
)
sealed class UpdateProductDto(
    @field:NotBlank
    val name: String?,
    val description: String?,
    val price: Long?,
) {
    val type: ProductType
        get() =
            when (this) {
                is Skate -> ProductType.SKATE
                is Deck -> ProductType.DECK
                is Wheel -> ProductType.WHEEL
                is Bearing -> ProductType.BEARING
                is GridTape -> ProductType.GRID_TAPE
                is Truck -> ProductType.TRUCK
            }

    class Skate(
        name: String?,
        description: String?,
        price: Long?,
    ) : UpdateProductDto(name, description, price)

    class Deck(
        name: String?,
        description: String?,
        price: Long?,
    ) : UpdateProductDto(name, description, price)

    class Wheel(
        name: String?,
        description: String?,
        price: Long?,
    ) : UpdateProductDto(name, description, price)

    class Bearing(
        name: String?,
        description: String?,
        price: Long?,
    ) : UpdateProductDto(name, description, price)

    class GridTape(
        name: String?,
        description: String?,
        price: Long?,
    ) : UpdateProductDto(name, description, price)

    class Truck(
        name: String?,
        description: String?,
        price: Long?,
    ) : UpdateProductDto(name, description, price)
}

class ProductPageDto(
    @Suppress("unused")
    val products: List<ProductDto>,
    @Suppress("unused")
    val pages: Long,
)

class ProductPaginationDto(
    @Suppress("unused")
    val products: List<ProductDto>,
    @Suppress("unused")
    val hasMore: Boolean,
)
