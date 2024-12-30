package com.flip.skateshop.mapper

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.extention.normalizedEmail
import com.flip.skateshop.web.rest.dto.AddressDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ShortUserDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.flip.skateshop.web.rest.dto.UserDto
import org.bson.types.ObjectId
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserMapper(
    private val passwordEncoder: PasswordEncoder,
    properties: SkateshopProperties,
    private val fileMapper: FileMapper,
) {
    private val keyValidity = properties.security.verificationKey.validityInSeconds

    fun toUser(
        registerDto: RegisterDto,
        activationKey: String,
    ) = registerDto.run {
        User(
            ObjectId(),
            firstName.trim().lowercase().replaceFirstChar { it.uppercase() },
            lastName.trim().uppercase(),
            email.trim().normalizedEmail(),
            null,
            null,
            passwordEncoder.encode(password),
            emptySet(),
            null,
            VerificationKey(activationKey, Instant.now().plusSeconds(keyValidity)),
            null,
            false,
            emptyMap(),
            emptyMap(),
        )
    }

    suspend fun toUserDto(user: User): UserDto =
        user.run {
            UserDto(
                _id.toHexString(),
                firstName,
                lastName,
                email,
                phone,
                address?.run {
                    AddressDto(
                        line1,
                        line2,
                        zipCode,
                        city,
                    )
                },
                logo?.let { fileMapper.toPrivatePath(it) },
            )
        }

    fun toValidUpdateUserDto(userDto: UpdateUserDto): UpdateUserDto =
        userDto.run {
            UpdateUserDto(
                firstName.trim(),
                lastName.trim(),
                phone,
                AddressDto(
                    address.line1.trim(),
                    address.line2.trim(),
                    address.zipCode,
                    address.city.trim().replaceFirstChar { it.uppercase() },
                ),
            )
        }

    fun toShortUserDto(user: User): ShortUserDto =
        user.run {
            ShortUserDto(_id.toHexString(), firstName, lastName, email, phone)
        }
}
