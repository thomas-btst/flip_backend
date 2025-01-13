package com.flip.skateshop.web.rest

import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.web.rest.dto.ShortUserDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.flip.skateshop.web.rest.dto.UserDto
import com.flip.skateshop.web.rest.dto.UserPageDto
import com.flip.skateshop.web.rest.dto.UsersStatsDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserServiceInterface,
    private val userMapper: UserMapper,
) {
    @GetMapping("/current")
    @Operation(summary = "Retrieve user information")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getCurrentUser(): UserDto = userMapper.toUserDto(userService.getCurrentUser())

    @PutMapping
    @Operation(summary = "Update user profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(ApiResponse(responseCode = "204"))
    suspend fun updateCurrentUserProfile(
        @RequestBody @Valid userDto: UpdateUserDto,
    ) = userService.updateProfileForCurrentUser(userDto)

    @PatchMapping("/logo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Update user logo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(ApiResponse(responseCode = "204"))
    suspend fun updateUserLogo(
        @RequestPart("logo") logo: FilePart,
    ) = userService.updateLogoForCurrentUser(logo)

    @GetMapping
    @Operation(summary = "Retrieve a pagination of users by email")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getUsersByPage(
        @RequestParam limit: Int,
        @RequestParam page: Long,
        @RequestParam search: String = "",
    ): UserPageDto = userService.getUsersByPage(limit, page, search)

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve an user by id")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "User not found"),
    )
    suspend fun getUser(
        @PathVariable id: ObjectId,
    ): ShortUserDto = userService.getUser(id)

    @GetMapping("/stats")
    @Operation(summary = "Retrieve users stats")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getUserStats(): UsersStatsDto = userService.getStats()
}
