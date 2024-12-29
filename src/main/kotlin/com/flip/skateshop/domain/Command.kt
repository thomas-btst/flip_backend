package com.flip.skateshop.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(Command.DOCUMENT_NAME)
sealed class Command(
    @Id
    val _id: ObjectId,
    val paymentId: String,
    val userId: ObjectId,
    val date: Instant,
    val address: Address,
    val products: Map<ObjectId, Long>,
    val total: Long,
) {
    companion object {
        const val DOCUMENT_NAME = "commands"
    }

    @TypeAlias(Paid.CLASS_NAME)
    class Paid(
        _id: ObjectId,
        paymentId: String,
        userId: ObjectId,
        val invoice: String,
        val status: CommandStatus,
        date: Instant,
        address: Address,
        products: Map<ObjectId, Long>,
        total: Long,
    ) : Command(_id, paymentId, userId, date, address, products, total) {
        companion object {
            const val CLASS_NAME = "PAID"
        }
    }

    @TypeAlias(UnPaid.CLASS_NAME)
    class UnPaid(
        _id: ObjectId,
        paymentId: String,
        userId: ObjectId,
        date: Instant,
        address: Address,
        products: Map<ObjectId, Long>,
        total: Long,
    ) : Command(_id, paymentId, userId, date, address, products, total) {
        companion object {
            const val CLASS_NAME = "UNPAID"
        }
    }
}

enum class CommandStatus {
    PENDING,
    IN_TRANSIT,
    DELIVERED,
    CANCELED,
}
