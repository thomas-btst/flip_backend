package com.flip.skateshop.web.rest

import com.flip.skateshop.service.UserService
import com.flip.skateshop.web.rest.dto.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val userService: UserService,
) {
    @PostMapping("/login")
    suspend fun login(@RequestBody @Valid loginDto: LoginDto): TokenDto = userService.login(loginDto)

    @PostMapping("/register")
    suspend fun register(@RequestBody @Valid registerDto: RegisterDto): ObjectId = userService.register(registerDto)

    @PostMapping("/activate/send/{email}")
    suspend fun sendActivationKey(@PathVariable @Email email: String) = userService.sendActivationKey(email)

    @PostMapping("/activate")
    suspend fun activate(@RequestBody @Valid activationDto: ActivationDto): TokenDto =
        activationDto.run { userService.activate(email, activationKey) }

    @PostMapping("/reset-password/send/{email}")
    suspend fun sendResetPassword(@PathVariable @Email email: String) = userService.sendResetPasswordKey(email)

    @PostMapping("/reset-password")
    suspend fun resetPassword(@RequestBody @Valid resetPasswordDto: ResetPasswordDto): TokenDto =
        userService.resetPassword(resetPasswordDto)
}