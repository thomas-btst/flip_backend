package com.flip.skateshop.repository

import com.flip.skateshop.domain.Feedback
import com.flip.skateshop.domain.FeedbackId
import com.flip.skateshop.interfaces.repository.FeedbackRepositoryInterface
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.springframework.data.mapping.div
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedbackCRUDRepository : CoroutineCrudRepository<Feedback, FeedbackId> {
    @Suppress("FunctionName")
    suspend fun findBy_idProductId(productId: ObjectId): List<Feedback>

    @Suppress("FunctionName")
    suspend fun deleteBy_id(feedbackId: FeedbackId)
}

class ProductRate(
    val rate: Double,
)

@Repository
class FeedbackRepository(
    private val repository: FeedbackCRUDRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : FeedbackRepositoryInterface {
    override suspend fun save(feedback: Feedback) = repository.save(feedback)

    override suspend fun delete(feedbackId: FeedbackId) = repository.deleteBy_id(feedbackId)

    override suspend fun findByProductId(productId: ObjectId) = repository.findBy_idProductId(productId)

    override suspend fun getRateAverageForProduct(productId: ObjectId): Double? {
        val aggregation =
            Aggregation.newAggregation(
                Aggregation.match(Criteria.where((Feedback::_id / FeedbackId::productId).toDotPath()).`is`(productId)),
                Aggregation.group().avg(Feedback::rate.name).`as`("rate"),
            )
        return mongoTemplate.aggregate<ProductRate>(aggregation, Feedback.DOCUMENT_NAME).awaitFirstOrNull()?.let { rate -> rate.rate }
    }
}
