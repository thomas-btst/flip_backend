package com.flip.skateshop.web.rest.dto

import com.flip.skateshop.domain.Address
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import validator.PhoneFormat

class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val address: Address?,
    val logo: String?,
)

class UpdateUserDto(
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:PhoneFormat
    val phone: String,
    @field:Valid
    val address: Address,
)
