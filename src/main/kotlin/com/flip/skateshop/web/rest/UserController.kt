package com.flip.skateshop.web.rest

import com.flip.skateshop.service.UserService
import com.flip.skateshop.web.rest.dto.CreateUserDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    val userService: UserService,
) {
    @GetMapping
    suspend fun getUsers() = userService.getUsers()

    @PostMapping
    suspend fun addUser(@RequestBody userDto: CreateUserDto) = userService.addUser(userDto)
}