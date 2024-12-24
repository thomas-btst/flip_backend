package com.flip.skateshop.repository

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.web.rest.dto.UpdateProductDto
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.lt
import org.springframework.data.mongodb.core.query.regex
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductCRUDRepository : CoroutineCrudRepository<Product, ObjectId> {
    @Suppress("FunctionName")
    suspend fun findBy_idIn(_id: Collection<ObjectId>): Flow<Product>
}

@Repository
class ProductRepository(
    private val repository: ProductCRUDRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : ProductRepositoryInterface {
    override suspend fun findByIdIn(productIds: Collection<ObjectId>): Flow<Product> = repository.findBy_idIn(productIds)

    override suspend fun save(product: Product): Product = repository.save(product)

    override suspend fun updateProduct(
        productId: ObjectId,
        productDto: UpdateProductDto,
    ): UpdateResult {
        val query = Query(Product::_id isEqualTo productId)
        val update =
            Update().apply {
                productDto.run {
                    if (name != null) {
                        set(Product::name.name, name)
                    }
                    if (description != null) {
                        set(Product::description.name, description)
                    }
                    if (price != null) {
                        set(Product::price.name, price)
                    }
                    set("_class", type)
                }
            }
        return mongoTemplate.updateFirst(query, update, Product::class.java).awaitSingle()
    }

    override suspend fun updateProductPicture(
        productId: ObjectId,
        picture: String,
    ): UpdateResult {
        val query = Query(Product::_id isEqualTo productId)
        val update = Update().set(Product::picture.name, picture)
        return mongoTemplate.updateFirst(query, update, Product::class.java).awaitSingle()
    }

    override suspend fun deleteById(productId: ObjectId) = repository.deleteById(productId)

    override suspend fun count(): Long = repository.count()

    override suspend fun findById(id: ObjectId): Product? = repository.findById(id)

    override suspend fun findByNameLikeAndByTypeAndByPage(
        limit: Int,
        page: Long,
        search: String,
        type: ProductType?,
    ): Pair<Flow<Product>, Long> {
        val query =
            Query().apply {
                if (search.isNotEmpty()) {
                    addCriteria(Product::name regex "(?i).*$search.*")
                }
                if (type != null) {
                    addCriteria(Criteria.where("_class").`is`(type))
                }
            }
        val count = mongoTemplate.count(query, Product::class.java).awaitSingle()
        query.apply {
            skip(limit * page)
            limit(limit)
            with(Sort.by(Sort.Direction.DESC, "_id"))
        }
        val products = mongoTemplate.find(query.skip(limit * page).limit(limit), Product::class.java).asFlow()
        return Pair(products, count)
    }

    override suspend fun findByFilterPaginated(
        limit: Int,
        pagination: ObjectId?,
        types: Set<ProductType>,
        minPrice: Long?,
        maxPrice: Long?,
        search: String,
    ): Pair<List<Product>, Boolean> {
        val query =
            Query().apply {
                if (pagination != null) {
                    addCriteria(Product::_id lt pagination)
                }
                if (minPrice != null || maxPrice != null) {
                    addCriteria(
                        Criteria.where(Product::price.name).apply {
                            if (minPrice != null) {
                                gte(minPrice)
                            }
                            if (maxPrice != null) {
                                lte(maxPrice)
                            }
                        },
                    )
                }
                if (search.isNotEmpty()) {
                    addCriteria(Product::name regex "(?i).*$search.*")
                }
                if (types.isNotEmpty()) {
                    addCriteria(Criteria.where("_class").`in`(types.map { it.name }))
                }
                with(Sort.by(Sort.Direction.DESC, "_id"))
                limit(limit + 1)
            }
        val products = mongoTemplate.find(query, Product::class.java).asFlow().toList()
        val hasMore = products.size > limit
        return Pair(products.take(limit), hasMore)
    }
}
