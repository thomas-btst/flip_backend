package com.flip.skateshop.mapper

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.extention.normalizedEmail
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.UserDto
import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserMapper(
    private val passwordEncoder: PasswordEncoder,
    properties: SkateshopProperties,
) {
    private val keyValidity = properties.security.verificationKey.validityInSeconds

    fun toUser(
        registerDto: RegisterDto,
        activationKey: String,
    ) = registerDto.run {
        User(
            ObjectId(),
            firstName.lowercase().replaceFirstChar { it.uppercase() },
            lastName.uppercase(),
            email.normalizedEmail(),
            passwordEncoder.encode(password),
            emptySet(),
            VerificationKey(activationKey, Instant.now().plusSeconds(keyValidity)),
            null,
            false,
        )
    }

    fun toUserDto(user: User): UserDto =
        user.run {
            UserDto(_id.toHexString(), firstName, lastName, email)
        }
}
