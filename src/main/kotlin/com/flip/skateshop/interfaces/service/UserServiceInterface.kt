package com.flip.skateshop.interfaces.service

import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import com.flip.skateshop.web.rest.dto.TokenDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart

interface UserServiceInterface {
    suspend fun getCurrentUser(): User

    suspend fun updateCurrentUserProfile(userDto: UpdateUserDto)

    suspend fun updateCurrentUserLogo(logo: FilePart)

    fun createToken(
        userId: ObjectId,
        authorities: List<RoleEnum>,
    ): TokenDto

    suspend fun login(loginDto: LoginDto): TokenDto

    suspend fun register(registerDto: RegisterDto)

    suspend fun sendActivationKey(email: String)

    suspend fun activate(
        email: String,
        activationKey: String,
    ): TokenDto

    suspend fun sendResetPasswordKey(email: String)

    suspend fun resetPassword(reset: ResetPasswordDto): TokenDto
}
