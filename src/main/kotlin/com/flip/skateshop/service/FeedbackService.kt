package com.flip.skateshop.service

import com.flip.skateshop.domain.FeedbackId
import com.flip.skateshop.interfaces.repository.FeedbackRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.FeedbackServiceInterface
import com.flip.skateshop.mapper.FeedbackMapper
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.web.rest.dto.CreateFeedbackDto
import com.flip.skateshop.web.rest.dto.FeedbackDto
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepositoryInterface,
    private val securityUtils: SecurityUtils,
    private val feedbackMapper: FeedbackMapper,
    private val userRepository: UserRepositoryInterface,
) : FeedbackServiceInterface {
    override suspend fun retrieveProductFeedbacks(productId: ObjectId): List<FeedbackDto> {
        val feedbacks = feedbackRepository.findByProductId(productId)
        val users = userRepository.findByIdIn(feedbacks.map { it._id.userId })
        return feedbacks.map { feedback ->
            val user =
                users.firstOrNull {
                    it._id == feedback._id.userId
                }
            feedbackMapper.toFeedbackDto(feedback, user)
        }
    }

    override suspend fun sendFeedbackForCurrentUser(
        productId: ObjectId,
        feedbackDto: CreateFeedbackDto,
    ) {
        val currentUserId = securityUtils.getCurrentUserId()
        val feedback = feedbackMapper.toFeedback(feedbackDto, currentUserId, productId)
        feedbackRepository.save(feedback)
    }

    override suspend fun deleteFeedbackForProductForCurrentUser(productId: ObjectId) {
        val currentUserId = securityUtils.getCurrentUserId()
        val feedbackId = FeedbackId(currentUserId, productId)
        feedbackRepository.delete(feedbackId)
    }
}
