package com.flip.skateshop.domain

import com.flip.skateshop.domain.Feedback.Companion.DOCUMENT_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(DOCUMENT_NAME)
class Feedback(
    @Id
    val _id: FeedbackId,
    val rate: Double,
    val comment: String,
    val date: Instant,
) {
    companion object {
        const val DOCUMENT_NAME = "feedbacks"
    }
}

class FeedbackId(
    val userId: ObjectId,
    val productId: ObjectId,
)
