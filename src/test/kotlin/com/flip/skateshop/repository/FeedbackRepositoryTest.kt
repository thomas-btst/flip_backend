package com.flip.skateshop.repository

import com.flip.skateshop.domain.Feedback
import com.flip.skateshop.domain.FeedbackId
import com.flip.skateshop.util.ServicesCleaner
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import kotlin.test.assertEquals

class FeedbackRepositoryTest(
    @Autowired
    private val feedbackRepository: FeedbackRepository,
) : ServicesCleaner() {
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
    fun `should save and retrieve a feedback successfully`() =
        runTest {
            val feedback = initFeedback()
            feedbackRepository.save(feedback)
            val feedbacks = feedbackRepository.findByProductId(feedback._id.productId)
            assertEquals(1, feedbacks.size)
            feedbacks.first().run {
                assertEquals(_id.userId, feedback._id.userId)
                assertEquals(_id.productId, feedback._id.productId)
                assertEquals(rate, feedback.rate)
                assertEquals(comment, feedback.comment)
            }
        }

    @Test
    fun `should update a feedback correctly`() =
        runTest {
            val feedback = initFeedback()
            feedbackRepository.save(feedback)
            val updatedFeedback = initFeedback(feedback._id, 5.0, "new commentary", Instant.now())
            feedbackRepository.save(updatedFeedback)
            val feedbacks = feedbackRepository.findByProductId(feedback._id.productId)
            assertEquals(1, feedbacks.size)
            feedbacks.first().run {
                assertEquals(_id.userId, updatedFeedback._id.userId)
                assertEquals(_id.productId, updatedFeedback._id.productId)
                assertEquals(rate, updatedFeedback.rate)
                assertEquals(comment, updatedFeedback.comment)
            }
        }

    @Test
    fun `should delete a feedback correctly`() =
        runTest {
            val feedback = initFeedback()
            feedbackRepository.save(feedback)
            feedbackRepository.delete(feedback._id)
            val feedbacks = feedbackRepository.findByProductId(feedback._id.productId)
            assertEquals(0, feedbacks.size)
        }

    @Test
    fun `should retrieve all feedbacks of a product`() =
        runTest {
            val productId = ObjectId()
            val count = 5
            for (i in 0..<count) {
                feedbackRepository.save(initFeedback(_id = FeedbackId(ObjectId(), productId)))
            }
            val feedbacks = feedbackRepository.findByProductId(productId)
            assertEquals(count, feedbacks.size)
        }

    @Test
    fun `should retrieve product rate average correctly`() =
        runTest {
            val productId = ObjectId()
            val count = 5
            var sum = 0.0
            for (i in 0..<count) {
                val rate = Math.random() * 5
                sum += rate
                feedbackRepository.save(initFeedback(_id = FeedbackId(ObjectId(), productId), rate = rate))
            }
            val rate = feedbackRepository.getRateAverageForProduct(productId)
            assertEquals(sum / count, rate)
        }
}
