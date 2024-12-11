package com.flip.skateshop.interfaces.repository

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import org.bson.types.ObjectId

interface ProductRepositoryInterface {
    suspend fun save(product: Product): Product

    suspend fun count(): Long

    suspend fun findById(id: ObjectId): Product?

    suspend fun findByFilterPaginated(
        limit: Int,
        pagination: ObjectId?,
        types: Set<ProductType>,
        minPrice: Long?,
        maxPrice: Long?,
        search: String,
    ): Pair<List<Product>, Boolean>
}
