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
    val password: String,
    val roles: Set<RoleEnum>,
    val activationKey: VerificationKey?,
    val resetPasswordKey: VerificationKey?,
    val enabled: Boolean,
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
