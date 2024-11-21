package com.flip.skateshop.web.rest

import com.flip.skateshop.service.UserService
import com.flip.skateshop.web.rest.dto.ActivationDto
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import com.flip.skateshop.web.rest.dto.TokenDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val userService: UserService,
) {
    @PostMapping("/login")
    @Operation(summary = "Authenticate an user and retrieve a token")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "401", description = "Login or password is incorrect"),
        ApiResponse(responseCode = "403", description = "Account is not activated yet"),
    )
    suspend fun login(
        @RequestBody @Valid loginDto: LoginDto,
    ): TokenDto = userService.login(loginDto)

    @PostMapping("/register")
    @Operation(summary = "Register an user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        ApiResponse(responseCode = "204"),
        ApiResponse(responseCode = "409", description = "Email is already used"),
    )
    suspend fun register(
        @RequestBody @Valid registerDto: RegisterDto,
    ) {
        userService.register(registerDto)
    }

    @PostMapping("/activate/send/{email}")
    @Operation(summary = "Send activation email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(ApiResponse(responseCode = "204"))
    suspend fun sendActivationKey(
        @PathVariable @Email email: String,
    ) {
        userService.sendActivationKey(email)
    }

    @PostMapping("/activate")
    @Operation(summary = "Activate user account")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", description = "Activation key is incorrect or has expired"),
    )
    suspend fun activate(
        @RequestBody @Valid activationDto: ActivationDto,
    ): TokenDto = activationDto.run { userService.activate(email, activationKey) }

    @PostMapping("/reset-password/send/{email}")
    @Operation(summary = "Send reset password email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(ApiResponse(responseCode = "204"))
    suspend fun sendResetPassword(
        @PathVariable @Email email: String,
    ) {
        userService.sendResetPasswordKey(email)
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "403", description = "Verification key is incorrect or has expired"),
    )
    suspend fun resetPassword(
        @RequestBody @Valid resetPasswordDto: ResetPasswordDto,
    ): TokenDto = userService.resetPassword(resetPasswordDto)
}
