package com.flip.skateshop.web.rest.dto

import com.flip.skateshop.domain.RoleEnum
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
    val token: String,
    @Suppress("unused")
    val authorities: List<RoleEnum>
)

class RegisterDto(
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:Email
    val email: String,
    @field:NotBlank
    val password: String,
)