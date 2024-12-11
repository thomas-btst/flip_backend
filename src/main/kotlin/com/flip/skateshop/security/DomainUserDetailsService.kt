package com.flip.skateshop.security

import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DomainUserDetailsService(
    private val userRepository: UserRepositoryInterface,
) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> =
        mono { userRepository.findOneByEmail(username) }
            .map { DomainUserDetails(it) }
}
