package com.flip.skateshop.interfaces.service

import com.flip.skateshop.web.rest.dto.CreateFeedbackDto
import com.flip.skateshop.web.rest.dto.FeedbackDto
import org.bson.types.ObjectId

interface FeedbackServiceInterface {
    suspend fun retrieveProductFeedbacks(productId: ObjectId): List<FeedbackDto>

    suspend fun sendFeedbackForCurrentUser(
        productId: ObjectId,
        feedbackDto: CreateFeedbackDto,
    )

    suspend fun deleteFeedbackForProductForCurrentUser(productId: ObjectId)
}
