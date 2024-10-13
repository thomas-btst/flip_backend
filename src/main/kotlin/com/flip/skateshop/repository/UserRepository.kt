package com.flip.skateshop.repository

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.springframework.data.mapping.div
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface UserRepository : CoroutineCrudRepository<User, ObjectId> {
    suspend fun findOneByEmail(email: String): User?
}

@Repository
class UserRepositoryWrapper(
    val repository: UserRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
    skateshopProperties: SkateshopProperties,
) {
    private val verificationKeyValidity = skateshopProperties.security.verificationKey.validityInSeconds

    private fun keyExpiration() = Instant.now().plusSeconds(verificationKeyValidity)

    private suspend fun ReactiveMongoTemplate.findAndModify(query: Query, update: Update) =
        findAndModify(query, update, User::class.java).awaitSingleOrNull()

    suspend fun updateResetPasswordKey(email: String, key: String): User? {
        val query = Query(User::email isEqualTo email)
        val update = Update().apply {
            set((User::resetPasswordKey / VerificationKey::key).toDotPath(), key)
            set((User::resetPasswordKey / VerificationKey::expiration).toDotPath(), keyExpiration())
        }
        return mongoTemplate.findAndModify(query, update)
    }

    suspend fun updateActivationKey(email: String, key: String): User? {
        val query = Query().apply {
            addCriteria(User::email isEqualTo email)
            addCriteria(User::enabled isEqualTo false)
        }
        val update = Update().apply {
            set((User::activationKey / VerificationKey::key).toDotPath(), key)
            set((User::activationKey / VerificationKey::expiration).toDotPath(), keyExpiration())
        }
        return mongoTemplate.findAndModify(query, update)
    }

    suspend fun activateAccountWithKey(email: String, activationKey: String): User? {
        val query = Query().apply {
            addCriteria(User::email isEqualTo email)
            addCriteria(Criteria.where((User::activationKey / VerificationKey::key).toDotPath()).`is`(activationKey))
            addCriteria(
                Criteria.where((User::activationKey / VerificationKey::expiration).toDotPath()).gt(Instant.now())
            )
        }
        val update = Update().apply {
            unset(User::activationKey.name)
            set(User::enabled.name, true)
        }
        return mongoTemplate.findAndModify(query, update)
    }

    suspend fun updatePasswordAndActivateWithKey(email: String, resetPasswordKey: String, newPassword: String): User? {
        val query = Query().apply {
            addCriteria(User::email isEqualTo email)
            addCriteria(
                Criteria.where((User::resetPasswordKey / VerificationKey::key).toDotPath()).`is`(resetPasswordKey)
            )
            addCriteria(
                Criteria.where((User::resetPasswordKey / VerificationKey::expiration).toDotPath()).gt(Instant.now())
            )
        }
        val update = Update().apply {
            unset(User::resetPasswordKey.name)
            set(User::password.name, newPassword)
            set(User::enabled.name, true)
        }
        return mongoTemplate.findAndModify(query, update)
    }
}