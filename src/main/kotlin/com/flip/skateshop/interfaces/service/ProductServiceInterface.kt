package com.flip.skateshop.interfaces.service

import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.ProductDto
import com.flip.skateshop.web.rest.dto.ProductPaginationDto
import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart

interface ProductServiceInterface {
    suspend fun getProduct(productId: ObjectId): ProductDto

    suspend fun getProducts(
        limit: Int,
        pagination: ObjectId?,
        types: Set<ProductType>,
        minPrice: Long?,
        maxPrice: Long?,
        search: String,
    ): ProductPaginationDto

    suspend fun addProduct(
        productDto: CreateProductDto,
        picture: FilePart,
    ): ObjectId
}
