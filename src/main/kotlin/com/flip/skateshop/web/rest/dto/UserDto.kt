package com.flip.skateshop.web.rest.dto

import com.flip.skateshop.validator.PhoneFormat
import com.flip.skateshop.validator.ZipCodeFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

class AddressDto(
    @field:NotBlank
    val line1: String,
    val line2: String,
    @field:ZipCodeFormat
    val zipCode: String,
    @field:NotBlank
    val city: String,
)

class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val address: AddressDto?,
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
    val address: AddressDto,
)

class UserPageDto(
    val users: List<ShortUserDto>,
    val pages: Long,
)

class ShortUserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
)

class UsersStatsDto(
    val count: Long,
)
