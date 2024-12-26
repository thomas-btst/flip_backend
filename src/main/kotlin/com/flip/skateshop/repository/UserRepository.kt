package com.flip.skateshop.repository

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.springframework.data.mapping.div
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.update
import org.springframework.data.mongodb.core.updateFirst
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface UserCRUDRepository : CoroutineCrudRepository<User, ObjectId> {
    suspend fun findOneByEmail(email: String): User?
}

@Repository
class UserRepository(
    private val repository: UserCRUDRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
    skateshopProperties: SkateshopProperties,
) : UserRepositoryInterface {
    private val verificationKeyValidity = skateshopProperties.security.verificationKey.validityInSeconds

    private fun keyExpiration() = Instant.now().plusSeconds(verificationKeyValidity)

    private suspend fun ReactiveMongoTemplate.findAndModify(
        query: Query,
        update: Update,
    ) = findAndModify(query, update, User::class.java).awaitSingleOrNull()

    override suspend fun save(user: User): User = repository.save(user)

    override suspend fun count(): Long = repository.count()

    override suspend fun findById(id: ObjectId): User? = repository.findById(id)

    override suspend fun findOneByEmail(email: String): User? = repository.findOneByEmail(email)

    override suspend fun updateResetPasswordKey(
        email: String,
        key: String,
    ): User? {
        val query = Query(User::email isEqualTo email)
        val update =
            Update().apply {
                set((User::resetPasswordKey / VerificationKey::key).toDotPath(), key)
                set((User::resetPasswordKey / VerificationKey::expiration).toDotPath(), keyExpiration())
            }
        return mongoTemplate.findAndModify(query, update)
    }

    override suspend fun updateActivationKey(
        email: String,
        key: String,
    ): User? {
        val query =
            Query().apply {
                addCriteria(User::email isEqualTo email)
                addCriteria(User::enabled isEqualTo false)
            }
        val update =
            Update().apply {
                set((User::activationKey / VerificationKey::key).toDotPath(), key)
                set((User::activationKey / VerificationKey::expiration).toDotPath(), keyExpiration())
            }
        return mongoTemplate.findAndModify(query, update)
    }

    override suspend fun activateAccountWithKey(
        email: String,
        key: String,
    ): User? {
        val query =
            Query().apply {
                addCriteria(User::email isEqualTo email)
                addCriteria(Criteria.where((User::activationKey / VerificationKey::key).toDotPath()).`is`(key))
                addCriteria(
                    Criteria.where((User::activationKey / VerificationKey::expiration).toDotPath()).gt(Instant.now()),
                )
            }
        val update =
            Update().apply {
                unset(User::activationKey.name)
                set(User::enabled.name, true)
            }
        return mongoTemplate.findAndModify(query, update)
    }

    override suspend fun updatePasswordAndActivateWithKey(
        email: String,
        key: String,
        newPassword: String,
    ): User? {
        val query =
            Query().apply {
                addCriteria(User::email isEqualTo email)
                addCriteria(
                    Criteria.where((User::resetPasswordKey / VerificationKey::key).toDotPath()).`is`(key),
                )
                addCriteria(
                    Criteria.where((User::resetPasswordKey / VerificationKey::expiration).toDotPath()).gt(Instant.now()),
                )
            }
        val update =
            Update().apply {
                unset(User::resetPasswordKey.name)
                set(User::password.name, newPassword)
                set(User::enabled.name, true)
            }
        return mongoTemplate.findAndModify(query, update)
    }

    override suspend fun updateUserProfile(
        userId: ObjectId,
        userDto: UpdateUserDto,
    ) {
        val query = Query().addCriteria(User::_id isEqualTo userId)
        val update =
            Update().apply {
                set(User::firstName.name, userDto.firstName)
                set(User::lastName.name, userDto.lastName)
                set(User::phone.name, userDto.phone)
                set(User::address.name, userDto.address)
            }
        mongoTemplate.updateFirst(query, update, User::class.java).awaitFirst()
    }

    override suspend fun updateUserLogo(
        userId: ObjectId,
        logo: String,
    ) {
        val query = Query().addCriteria(User::_id isEqualTo userId)
        val update =
            Update().apply {
                set(User::logo.name, logo)
            }
        mongoTemplate.updateFirst(query, update, User::class.java).awaitFirst()
    }

    override suspend fun addToCart(
        userId: ObjectId,
        productId: ObjectId,
        quantity: Long,
    ) {
        val query = Query().addCriteria(User::_id isEqualTo userId)
        val update =
            Update().apply {
                set("${User::cart.name}.$productId", quantity)
            }
        mongoTemplate.updateFirst(query, update, User::class.java).awaitFirst()
    }

    override suspend fun removeFromCart(
        userId: ObjectId,
        productId: ObjectId,
    ): UpdateResult {
        val query = Query().addCriteria(User::_id isEqualTo userId)
        val update =
            Update().apply {
                unset("${User::cart.name}.$productId")
            }
        return mongoTemplate.updateFirst(query, update, User::class.java).awaitFirst()
    }

    override suspend fun clearCart(userId: ObjectId) {
        val query = Query().addCriteria(User::_id isEqualTo userId)
        val update =
            Update().apply {
                set(User::cart.name, emptyMap<ObjectId, Long>())
            }
        mongoTemplate.updateFirst(query, update, User::class.java).awaitFirst()
    }
}
