package com.flip.skateshop.service

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.extention.normalizedEmail
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.interfaces.service.MailServiceInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.security.DomainUserDetails
import com.flip.skateshop.security.JwtClaimer
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.utils.randomString
import com.flip.skateshop.web.rest.dto.AccessTokenDto
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RefreshTokenDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import com.flip.skateshop.web.rest.dto.ShortUserDto
import com.flip.skateshop.web.rest.dto.TokenDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.flip.skateshop.web.rest.dto.UserPageDto
import com.flip.skateshop.web.rest.dto.UsersStatsDto
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepositoryInterface,
    private val userMapper: UserMapper,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtClaimer: JwtClaimer,
    private val securityUtils: SecurityUtils,
    private val mailService: MailServiceInterface,
    private val passwordEncoder: PasswordEncoder,
    private val fileService: FileServiceInterface,
    private val properties: SkateshopProperties,
    @Qualifier("no_expiration")
    private val jwtDecoder: ReactiveJwtDecoder,
) : UserServiceInterface {
    override suspend fun getCurrentUser(): User {
        val currentUserId = securityUtils.getCurrentUserId()
        return userRepository.findById(currentUserId) ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User deleted")
    }

    override suspend fun updateProfileForCurrentUser(userDto: UpdateUserDto) {
        userRepository.updateUserProfile(
            securityUtils.getCurrentUserId(),
            userMapper.toValidUpdateUserDto(userDto),
        )
    }

    override suspend fun getUser(userId: ObjectId): ShortUserDto {
        val user =
            userRepository.findById(userId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        return userMapper.toShortUserDto(user)
    }

    override suspend fun updateLogoForCurrentUser(logo: FilePart) {
        val userId = securityUtils.getCurrentUserId()
        val path = fileService.putUserLogo(userId, logo)
        userRepository.updateUserLogo(userId, path)
    }

    fun createAccessToken(
        userId: ObjectId,
        authorities: List<RoleEnum>,
    ): String = jwtClaimer.createToken(userId.toHexString(), authorities.map { it.toString() })

    suspend fun createTokens(
        userId: ObjectId,
        authorities: List<RoleEnum>,
    ): TokenDto {
        val accessToken = createAccessToken(userId, authorities)
        val refreshToken = UUID.randomUUID().toString()
        val expiration = Instant.now().plusSeconds(properties.security.refreshToken.validityInSeconds)
        userRepository.addRefreshToken(
            userId,
            refreshToken,
            expiration,
        )
        return TokenDto(accessToken, refreshToken, authorities)
    }

    override suspend fun login(loginDto: LoginDto): TokenDto {
        val normalizedEmail = loginDto.email.normalizedEmail()
        val authentication =
            authenticationManager
                .authenticate(UsernamePasswordAuthenticationToken(normalizedEmail, loginDto.password))
                .awaitSingle()
        val principal = authentication.principal as DomainUserDetails

        if (!principal.enabled) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        return createTokens(principal.id, authentication.authorities.map { RoleEnum.valueOf(it.toString()) })
    }

    override suspend fun register(registerDto: RegisterDto) {
        if (userRepository.findOneByEmail(registerDto.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email is already used.")
        }
        val activationKey = randomString(6)
        val user = userRepository.save(userMapper.toUser(registerDto, activationKey))
        mailService.sendActivationKey(user.email, user.firstName, user.lastName, activationKey)
    }

    suspend fun decodeAccessToken(token: String): Jwt =
        try {
            jwtDecoder.decode(token).awaitFirst()
        } catch (e: BadJwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access token invalid")
        }

    override suspend fun refreshToken(refreshTokenDto: RefreshTokenDto): AccessTokenDto {
        val token = decodeAccessToken(refreshTokenDto.accessToken)
        val user =
            userRepository.findByIdAndRefreshTokenExistsAndNotExpired(ObjectId(token.subject), refreshTokenDto.refreshToken)
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalid or expired")
        return AccessTokenDto(createAccessToken(user._id, user.roles.toList()))
    }

    override suspend fun logout(tokenDto: RefreshTokenDto) {
        val token = decodeAccessToken(tokenDto.accessToken)
        userRepository.deleteRefreshToken(ObjectId(token.subject), tokenDto.refreshToken)
    }

    override suspend fun sendActivationKey(email: String) {
        val normalizedEmail = email.normalizedEmail()
        val activationKey = randomString(6)
        val user = userRepository.updateActivationKey(normalizedEmail, activationKey) ?: return
        mailService.sendActivationKey(user.email, user.firstName, user.lastName, activationKey)
    }

    override suspend fun activate(
        email: String,
        activationKey: String,
    ): TokenDto {
        val normalizedEmail = email.normalizedEmail()
        val user =
            userRepository.activateAccountWithKey(normalizedEmail, activationKey) ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
            )
        return createTokens(user._id, user.roles.toList())
    }

    override suspend fun sendResetPasswordKey(email: String) {
        val normalizedEmail = email.normalizedEmail()
        val resetPasswordKey = randomString(6)
        val user = userRepository.updateResetPasswordKey(normalizedEmail, resetPasswordKey) ?: return
        mailService.sendResetPasswordKey(user.email, user.firstName, user.lastName, resetPasswordKey)
    }

    override suspend fun resetPassword(reset: ResetPasswordDto): TokenDto {
        val normalizedEmail = reset.email.normalizedEmail()
        val user =
            userRepository.updatePasswordAndActivateWithKey(
                normalizedEmail,
                reset.verificationKey,
                passwordEncoder.encode(reset.newPassword),
            ) ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return createTokens(user._id, user.roles.toList())
    }

    override suspend fun getUsersByPage(
        limit: Int,
        page: Long,
        search: String,
    ): UserPageDto {
        val userPage = userRepository.findByEmailLikeAndByPage(limit, page, search)
        val pages =
            if (limit > 0) {
                (userPage.second + limit - 1) / limit
            } else {
                0L
            }
        return UserPageDto(userPage.first.map(userMapper::toShortUserDto).toList(), pages)
    }

    override suspend fun getStats(): UsersStatsDto {
        val count = userRepository.count()
        return UsersStatsDto(count)
    }
}
