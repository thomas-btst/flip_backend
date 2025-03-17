package com.flip.skateshop.interfaces.repository

import com.flip.skateshop.domain.Feedback
import com.flip.skateshop.domain.FeedbackId
import org.bson.types.ObjectId

interface FeedbackRepositoryInterface {
    suspend fun save(feedback: Feedback): Feedback

    suspend fun delete(feedbackId: FeedbackId)

    suspend fun findByProductId(productId: ObjectId): List<Feedback>

    suspend fun getRateAverageForProduct(productId: ObjectId): Double?
}
