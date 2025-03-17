package com.flip.skateshop.service

import com.flip.skateshop.domain.Feedback
import com.flip.skateshop.domain.FeedbackId
import com.flip.skateshop.interfaces.repository.FeedbackRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.FeedbackServiceInterface
import com.flip.skateshop.mapper.FeedbackMapper
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.CreateFeedbackDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import kotlin.test.assertEquals

class FeedbackServiceTest(
    @Autowired private val feedbackRepository: FeedbackRepositoryInterface,
    @Autowired private val userRepository: UserRepositoryInterface,
    @Autowired private val feedbackMapper: FeedbackMapper,
) : ServicesCleaner() {
    private val securityUtils = mockk<SecurityUtils>()

    private val feedbackService: FeedbackServiceInterface =
        FeedbackService(feedbackRepository, securityUtils, feedbackMapper, userRepository)

    fun initFeedback(
        _id: FeedbackId = FeedbackId(ObjectId(), ObjectId()),
        rate: Double = 4.6,
        comment: String = "commentary",
        date: Instant = Instant.now(),
    ) = Feedback(
        _id,
        rate,
        comment,
        date,
    )

    @Test
    fun `should add a feedback correctly`() =
        runTest {
            val productId = ObjectId()
            val feedbackDto = CreateFeedbackDto(3.5, "comment")
            val userId = ObjectId()
            coEvery { securityUtils.getCurrentUserId() } returns userId
            feedbackService.sendFeedbackForCurrentUser(productId, feedbackDto)
            val foundFeedback = feedbackRepository.findByProductId(productId)
            assertEquals(1, foundFeedback.size)
            foundFeedback.first().run {
                assertEquals(feedbackDto.rate, rate)
                assertEquals(feedbackDto.comment, comment)
                assertEquals(productId, _id.productId)
                assertEquals(userId, _id.userId)
            }
        }

    @Test
    fun `should retrieve all feedbacks of a product correctly`() =
        runTest {
            val productId = ObjectId()
            val count = 5
            for (i in 0..<count) {
                feedbackRepository.save(initFeedback(_id = FeedbackId(ObjectId(), productId)))
            }
            feedbackRepository.save(initFeedback())
            val foundFeedback = feedbackService.retrieveProductFeedbacks(productId)
            assertEquals(count, foundFeedback.size)
        }

    @Test
    fun `should delete a feedback successfully`() =
        runTest {
            val feedback = initFeedback()
            feedbackRepository.save(feedback)
            coEvery { securityUtils.getCurrentUserId() } returns feedback._id.userId
            feedbackService.deleteFeedbackForProductForCurrentUser(feedback._id.productId)
            assertEquals(0, feedbackRepository.findByProductId(feedback._id.productId).size)
        }
}
