package com.flip.skateshop.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(User.DOCUMENT_NAME)
class User(
    @Id
    val _id: ObjectId,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val address: Address?,
    val password: String,
    val roles: Set<RoleEnum>,
    val logo: String?,
    val activationKey: VerificationKey?,
    val resetPasswordKey: VerificationKey?,
    val enabled: Boolean,
    val cart: Map<ObjectId, Long>,
) {
    companion object {
        const val DOCUMENT_NAME = "users"
    }
}

enum class RoleEnum {
    ADMIN,
}

class VerificationKey(
    val key: String,
    val expiration: Instant,
)

class Address(
    val line1: String,
    val line2: String,
    val zipCode: String,
    val city: String,
)
