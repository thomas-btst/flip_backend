package com.flip.skateshop.web.rest

import com.flip.skateshop.interfaces.service.FeedbackServiceInterface
import com.flip.skateshop.web.rest.dto.CreateFeedbackDto
import com.flip.skateshop.web.rest.dto.FeedbackDto
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackServiceInterface,
) {
    @GetMapping("/products/{productId}")
    suspend fun retrieveFeedbacks(
        @PathVariable productId: ObjectId,
    ): List<FeedbackDto> = feedbackService.retrieveProductFeedbacks(productId)

    @PutMapping("/products/{productId}")
    suspend fun sendFeedback(
        @PathVariable productId: ObjectId,
        @Valid @RequestBody feedbackDto: CreateFeedbackDto,
    ) {
        feedbackService.sendFeedbackForCurrentUser(productId, feedbackDto)
    }

    @DeleteMapping("/products/{productId}")
    suspend fun deleteFeedback(
        @PathVariable productId: ObjectId,
    ) {
        feedbackService.deleteFeedbackForProductForCurrentUser(productId)
    }
}
