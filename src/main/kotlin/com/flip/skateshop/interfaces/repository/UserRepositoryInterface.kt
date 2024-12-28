package com.flip.skateshop.interfaces.repository

import com.flip.skateshop.domain.User
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import java.time.Instant

interface UserRepositoryInterface {
    suspend fun save(user: User): User

    suspend fun count(): Long

    suspend fun findById(id: ObjectId): User?

    suspend fun findOneByEmail(email: String): User?

    suspend fun updateResetPasswordKey(
        email: String,
        key: String,
    ): User?

    suspend fun updateActivationKey(
        email: String,
        key: String,
    ): User?

    suspend fun activateAccountWithKey(
        email: String,
        key: String,
    ): User?

    suspend fun updatePasswordAndActivateWithKey(
        email: String,
        key: String,
        newPassword: String,
    ): User?

    suspend fun updateUserProfile(
        userId: ObjectId,
        userDto: UpdateUserDto,
    )

    suspend fun updateUserLogo(
        userId: ObjectId,
        logo: String,
    )

    suspend fun addToCart(
        userId: ObjectId,
        productId: ObjectId,
        quantity: Long,
    )

    suspend fun removeFromCart(
        userId: ObjectId,
        productId: ObjectId,
    ): UpdateResult

    suspend fun clearCart(userId: ObjectId)

    suspend fun addRefreshToken(
        userId: ObjectId,
        token: String,
        expiration: Instant,
    ): UpdateResult

    suspend fun deleteRefreshToken(
        userId: ObjectId,
        token: String,
    ): UpdateResult

    suspend fun findByIdAndRefreshTokenExistsAndNotExpired(
        userId: ObjectId,
        token: String,
    ): User?

    suspend fun findByEmailLikeAndByPage(
        limit: Int,
        page: Long,
        search: String,
    ): Pair<Flow<User>, Long>
}
