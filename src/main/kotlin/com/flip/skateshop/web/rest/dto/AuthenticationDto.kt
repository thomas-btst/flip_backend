package com.flip.skateshop.web.rest.dto

import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.validator.PasswordFormat
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class LoginDto(
    @field:Email
    val email: String,
    @field:NotBlank
    val password: String,
)

class ResetPasswordDto(
    @field:Email
    val email: String,
    @field:NotBlank
    @field:PasswordFormat
    val newPassword: String,
    @field:NotBlank
    val verificationKey: String,
)

class ActivationDto(
    @field:Email
    val email: String,
    @field:NotBlank
    val activationKey: String,
)

class TokenDto(
    @Suppress("unused")
    val accessToken: String,
    val refreshToken: String,
    @Suppress("unused")
    val roles: List<RoleEnum>,
)

class RefreshTokenDto(
    val accessToken: String,
    val refreshToken: String,
)

class AccessTokenDto(
    val accessToken: String,
)

class RegisterDto(
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:Email
    val email: String,
    @field:NotBlank
    @field:PasswordFormat
    val password: String,
)
