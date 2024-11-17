package com.flip.skateshop.repository

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.lt
import org.springframework.data.mongodb.core.query.regex
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : CoroutineCrudRepository<Product, ObjectId>

@Repository
class ProductRepositoryWrapper(
    val repository: ProductRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    suspend fun findByFilterPaginated(
        limit: Int,
        pagination: ObjectId?,
        types: Set<ProductType>,
        minPrice: Long?,
        maxPrice: Long?,
        search: String,
    ): Pair<List<Product>, Boolean> {
        val query = Query().apply {
            if (pagination != null)
                addCriteria(Product::_id lt pagination)
            if (minPrice != null || maxPrice != null)
                addCriteria(Criteria.where(Product::price.name).apply {
                    if (minPrice != null)
                        gte(minPrice)
                    if (maxPrice != null)
                        lte(maxPrice)
                })
            if (search.isNotEmpty())
                addCriteria(Product::name regex "(?i).*$search.*")
            if (types.isNotEmpty())
                addCriteria(Criteria.where("_class").`in`(types.map { it.name }))
            with(Sort.by(Sort.Direction.DESC, "_id"))
            limit(limit + 1)
        }
        val products = mongoTemplate.find(query, Product::class.java).asFlow().toList()
        val hasMore = products.size > limit
        return Pair(products.take(limit), hasMore)
    }
}