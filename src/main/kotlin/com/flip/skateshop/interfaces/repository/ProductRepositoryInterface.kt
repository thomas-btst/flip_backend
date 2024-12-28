package com.flip.skateshop.interfaces.repository

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.web.rest.dto.UpdateProductDto
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId

interface ProductRepositoryInterface {
    suspend fun save(product: Product): Product

    suspend fun count(): Long

    suspend fun findById(id: ObjectId): Product?

    suspend fun deleteById(productId: ObjectId)

    suspend fun updateProduct(
        productId: ObjectId,
        productDto: UpdateProductDto,
    ): UpdateResult

    suspend fun updatePicture(
        productId: ObjectId,
        picture: String,
    ): UpdateResult

    suspend fun findByNameLikeAndByTypeAndByPage(
        limit: Int,
        page: Long,
        search: String,
        type: ProductType?,
    ): Pair<Flow<Product>, Long>

    suspend fun findByFilterPaginated(
        limit: Int,
        pagination: ObjectId?,
        types: Set<ProductType>,
        minPrice: Long?,
        maxPrice: Long?,
        search: String,
    ): Pair<List<Product>, Boolean>

    suspend fun findByIdIn(productIds: Collection<ObjectId>): Flow<Product>
}
