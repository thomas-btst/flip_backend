package com.flip.skateshop.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(Command.DOCUMENT_NAME)
class Command(
    @Id
    val _id: ObjectId,
    val userId: ObjectId,
    val invoice: String,
    val date: Instant,
    val address: Address,
    val products: Map<ObjectId, Long>,
    val total: Long,
    val status: CommandStatus,
) {
    companion object {
        const val DOCUMENT_NAME = "commands"
    }
}

enum class CommandStatus {
    PENDING,
    IN_TRANSIT,
    DELIVERED,
    CANCELED,
}
