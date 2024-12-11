package com.flip.skateshop.service

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
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import com.flip.skateshop.web.rest.dto.TokenDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

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
) : UserServiceInterface {
    override suspend fun getCurrentUser(): User {
        val currentUserId = securityUtils.getCurrentUserId()
        return userRepository.findById(currentUserId)!!
    }

    override suspend fun updateCurrentUserProfile(userDto: UpdateUserDto) {
        userRepository.updateUserProfile(
            securityUtils.getCurrentUserId(),
            userMapper.toValidUpdateUserDto(userDto),
        )
    }

    override suspend fun updateCurrentUserLogo(logo: FilePart) {
        val userId = securityUtils.getCurrentUserId()
        val path = fileService.putUserLogo(userId, logo)
        userRepository.updateUserLogo(userId, path)
    }

    override fun createToken(
        userId: ObjectId,
        authorities: List<RoleEnum>,
    ): TokenDto {
        val token = jwtClaimer.createToken(userId.toHexString(), authorities.map { it.toString() })
        return TokenDto(token, authorities)
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

        return createToken(principal.id, authentication.authorities.map { RoleEnum.valueOf(it.toString()) })
    }

    override suspend fun register(registerDto: RegisterDto) {
        if (userRepository.findOneByEmail(registerDto.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email is already used.")
        }
        val activationKey = randomString(6)
        val user = userRepository.save(userMapper.toUser(registerDto, activationKey))
        mailService.sendActivationKey(user.email, user.firstName, user.lastName, activationKey)
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
        return createToken(user._id, user.roles.toList())
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
        return createToken(user._id, user.roles.toList())
    }
}
