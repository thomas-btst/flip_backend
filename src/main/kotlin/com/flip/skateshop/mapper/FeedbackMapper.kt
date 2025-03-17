package com.flip.skateshop.mapper

import com.flip.skateshop.domain.Feedback
import com.flip.skateshop.domain.FeedbackId
import com.flip.skateshop.domain.User
import com.flip.skateshop.web.rest.dto.CreateFeedbackDto
import com.flip.skateshop.web.rest.dto.FeedbackDto
import com.flip.skateshop.web.rest.dto.FeedbackUserDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class FeedbackMapper(
    private val fileMapper: FileMapper,
) {
    fun toFeedback(
        feedbackDto: CreateFeedbackDto,
        userId: ObjectId,
        productId: ObjectId,
    ) = feedbackDto.run {
        Feedback(
            FeedbackId(userId, productId),
            rate,
            comment.trim(),
            Instant.now(),
        )
    }

    suspend fun toFeedbackDto(
        feedback: Feedback,
        user: User?,
    ) = feedback.run {
        FeedbackDto(
            user?.run { FeedbackUserDto(firstName, lastName, logo?.let { fileMapper.toPrivatePath(it) }) },
            rate,
            comment,
            date,
        )
    }
}
