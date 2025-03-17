package com.flip.skateshop.mapper

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.Product.*
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.ProductDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class ProductMapper(
    private val fileMapper: FileMapper,
) {
    fun toProductType(product: Product): ProductType =
        when (product) {
            is Skate -> ProductType.SKATE
            is Deck -> ProductType.DECK
            is Wheel -> ProductType.WHEEL
            is Bearing -> ProductType.BEARING
            is GridTape -> ProductType.GRID_TAPE
            is Truck -> ProductType.TRUCK
        }

    fun toProduct(
        productId: ObjectId,
        productDto: CreateProductDto,
        picture: String,
    ): Product =
        productDto.run {
            val trimedName = name.trim()
            val trimedDescription = description.trim()
            when (productDto) {
                is CreateProductDto.Skate -> Skate(ObjectId(), trimedName, trimedDescription, price, picture)
                is CreateProductDto.Deck -> Deck(ObjectId(), trimedName, trimedDescription, price, picture)
                is CreateProductDto.Wheel -> Wheel(ObjectId(), trimedName, trimedDescription, price, picture)
                is CreateProductDto.Bearing -> Bearing(ObjectId(), trimedName, trimedDescription, price, picture)
                is CreateProductDto.GridTape -> GridTape(ObjectId(), trimedName, trimedDescription, price, picture)
                is CreateProductDto.Truck -> Truck(ObjectId(), trimedName, trimedDescription, price, picture)
            }
        }

    fun toProductDto(
        product: Product,
        rate: Double? = null,
    ): ProductDto =
        product.run {
            val picturePath = fileMapper.toPublicPath(product.picture)
            when (product) {
                is Skate -> ProductDto.Skate(_id.toHexString(), name, description, price, picturePath, rate)
                is Deck -> ProductDto.Deck(_id.toHexString(), name, description, price, picturePath, rate)
                is Wheel -> ProductDto.Wheel(_id.toHexString(), name, description, price, picturePath, rate)
                is Bearing -> ProductDto.Bearing(_id.toHexString(), name, description, price, picturePath, rate)
                is GridTape -> ProductDto.GridTape(_id.toHexString(), name, description, price, picturePath, rate)
                is Truck -> ProductDto.Truck(_id.toHexString(), name, description, price, picturePath, rate)
            }
        }
}
