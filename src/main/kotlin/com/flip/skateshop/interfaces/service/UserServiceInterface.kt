package com.flip.skateshop.interfaces.service

import com.flip.skateshop.domain.User
import com.flip.skateshop.web.rest.dto.AccessTokenDto
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RefreshTokenDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import com.flip.skateshop.web.rest.dto.ShortUserDto
import com.flip.skateshop.web.rest.dto.TokenDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.flip.skateshop.web.rest.dto.UserPageDto
import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart

interface UserServiceInterface {
    suspend fun getCurrentUser(): User

    suspend fun updateProfileForCurrentUser(userDto: UpdateUserDto)

    suspend fun getUser(userId: ObjectId): ShortUserDto

    suspend fun updateLogoForCurrentUser(logo: FilePart)

    suspend fun login(loginDto: LoginDto): TokenDto

    suspend fun register(registerDto: RegisterDto)

    suspend fun sendActivationKey(email: String)

    suspend fun activate(
        email: String,
        activationKey: String,
    ): TokenDto

    suspend fun sendResetPasswordKey(email: String)

    suspend fun resetPassword(reset: ResetPasswordDto): TokenDto

    suspend fun refreshToken(refreshTokenDto: RefreshTokenDto): AccessTokenDto

    suspend fun logout(tokenDto: RefreshTokenDto)

    suspend fun getUsersByPage(
        limit: Int,
        page: Long,
        search: String,
    ): UserPageDto
}
