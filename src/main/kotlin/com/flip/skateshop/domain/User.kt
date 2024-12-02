package com.flip.skateshop.domain

import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import validator.ZipCodeFormat
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
    @field:NotBlank
    val line1: String,
    val line2: String,
    @field:ZipCodeFormat
    val zipCode: String,
    @field:NotBlank
    val city: String,
)
