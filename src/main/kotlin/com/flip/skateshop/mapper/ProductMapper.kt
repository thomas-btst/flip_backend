package com.flip.skateshop.mapper

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.Product.*
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.ProductDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class ProductMapper(private val fileMapper: FileMapper) {
    fun toProduct(productId: ObjectId, productDto: CreateProductDto, picture: String): Product = productDto.run {
        when (productDto) {
            is CreateProductDto.Skate -> Skate(ObjectId(), name, description, price, picture)
            is CreateProductDto.Deck -> Deck(ObjectId(), name, description, price, picture)
            is CreateProductDto.Wheel -> Wheel(ObjectId(), name, description, price, picture)
            is CreateProductDto.Bearing -> Bearing(ObjectId(), name, description, price, picture)
            is CreateProductDto.GridTape -> GridTape(ObjectId(), name, description, price, picture)
            is CreateProductDto.Truck -> Truck(ObjectId(), name, description, price, picture)
        }
    }

    fun toProductDto(product: Product): ProductDto = product.run {
        val picturePath = fileMapper.toPublicPath(product.picture)
        when (product) {
            is Skate -> ProductDto.Skate(_id.toHexString(), name, description, price, picturePath)
            is Deck -> ProductDto.Deck(_id.toHexString(), name, description, price, picturePath)
            is Wheel -> ProductDto.Wheel(_id.toHexString(), name, description, price, picturePath)
            is Bearing -> ProductDto.Bearing(_id.toHexString(), name, description, price, picturePath)
            is GridTape -> ProductDto.GridTape(_id.toHexString(), name, description, price, picturePath)
            is Truck -> ProductDto.Truck(_id.toHexString(), name, description, price, picturePath)
        }
    }
}