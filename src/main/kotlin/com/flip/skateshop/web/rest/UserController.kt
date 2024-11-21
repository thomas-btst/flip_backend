package com.flip.skateshop.web.rest

import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.service.UserService
import com.flip.skateshop.web.rest.dto.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    val userService: UserService,
    private val userMapper: UserMapper,
) {
    @GetMapping
    @Operation(summary = "Retrieve user information")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getCurrentUser(): UserDto = userMapper.toUserDto(userService.getCurrentUser())
}
