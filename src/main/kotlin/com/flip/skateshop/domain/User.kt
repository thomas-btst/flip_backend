package com.flip.skateshop.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(User.DOCUMENT_NAME)
class User(
    @Id
    val _id: ObjectId,
    val username: String,
    val email: String,
    val password: String,
) {
    companion object {
        const val DOCUMENT_NAME = "users"
    }
}