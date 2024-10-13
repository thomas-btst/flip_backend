package com.flip.skateshop.web.rest

import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.service.UserService
import com.flip.skateshop.web.rest.dto.*
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    val userService: UserService,
    private val userMapper: UserMapper,
) {
    @GetMapping
    suspend fun getCurrentUser(): UserDto =
        userMapper.toUserDto(userService.getCurrentUser())
}