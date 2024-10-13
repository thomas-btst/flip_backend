package com.flip.skateshop.security

import com.flip.skateshop.domain.User
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DomainUserDetailsPasswordService(
    private val mongoTemplate: ReactiveMongoTemplate
) : ReactiveUserDetailsPasswordService {
    override fun updatePassword(user: UserDetails, newPassword: String): Mono<UserDetails> {
        val query = Query(User::email isEqualTo user.username)
        val update = Update().set(User::password.name, newPassword)
        return mongoTemplate.updateFirst(query, update, User::class.java).map { user }
    }
}
