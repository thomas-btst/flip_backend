package com.flip.skateshop.web.rest.dto

class CreateUserDto(
    val username: String,
    val email: String,
    val password: String,
)

class UserDto(
    val id: String,
    val username: String,
    val email: String,
)