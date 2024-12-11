package com.flip.skateshop.web.rest

import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.flip.skateshop.web.rest.dto.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserServiceInterface,
    private val userMapper: UserMapper,
) {
    @GetMapping
    @Operation(summary = "Retrieve user information")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getCurrentUser(): UserDto = userMapper.toUserDto(userService.getCurrentUser())

    @PutMapping
    @Operation(summary = "Update user profile")
    @ApiResponses(ApiResponse(responseCode = "204"))
    suspend fun updateCurrentUserProfile(
        @RequestBody @Valid userDto: UpdateUserDto,
    ) = userService.updateCurrentUserProfile(userDto)

    @PutMapping("/logo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Update user logo")
    @ApiResponses(ApiResponse(responseCode = "204"))
    suspend fun updateUserLogo(
        @RequestPart("logo") logo: FilePart,
    ) = userService.updateCurrentUserLogo(logo)
}
