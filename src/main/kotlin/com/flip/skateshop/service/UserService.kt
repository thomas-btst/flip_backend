package com.flip.skateshop.service

import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.extention.normalizedEmail
import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.repository.UserRepositoryWrapper
import com.flip.skateshop.security.DomainUserDetails
import com.flip.skateshop.security.JwtClaimer
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.utils.randomString
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import com.flip.skateshop.web.rest.dto.TokenDto
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepositoryWrapper,
    private val userMapper: UserMapper,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtClaimer: JwtClaimer,
    private val securityUtils: SecurityUtils,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun getCurrentUser(): User {
        val currentUserId = securityUtils.getCurrentUserId()
        return userRepository.repository.findById(currentUserId)!!
    }

    fun createToken(userId: ObjectId, authorities: List<RoleEnum>): TokenDto {
        val token = jwtClaimer.createToken(userId.toHexString(), authorities.map { it.toString() })
        return TokenDto(token, authorities)
    }

    suspend fun login(loginDto: LoginDto): TokenDto {
        val normalizedEmail = loginDto.email.normalizedEmail()
        val authentication = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(normalizedEmail, loginDto.password))
            .awaitSingle()
        val principal = authentication.principal as DomainUserDetails

        if (!principal.enabled)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        return createToken(principal.id, authentication.authorities.map { RoleEnum.valueOf(it.toString()) })
    }

    suspend fun register(registerDto: RegisterDto): ObjectId {
        if(userRepository.repository.findOneByEmail(registerDto.email) != null)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email is already used.")
        val activationKey = randomString(6)
        return userRepository.repository.save(userMapper.toUser(registerDto, activationKey)).also { user ->
            mailService.sendActivationKey(user.email, user.firstName, user.lastName, activationKey)
        }._id

    }

    suspend fun sendActivationKey(email: String) {
        val normalizedEmail = email.normalizedEmail()
        val activationKey = randomString(6)
        val user = userRepository.updateActivationKey(normalizedEmail, activationKey) ?: return
        mailService.sendActivationKey(user.email, user.firstName, user.lastName, activationKey)
    }

    suspend fun activate(email: String, activationKey: String): TokenDto {
        val normalizedEmail = email.normalizedEmail()
        val user =
            userRepository.activateAccountWithKey(normalizedEmail, activationKey) ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN
            )
        return createToken(user._id, user.roles.toList())
    }

    suspend fun sendResetPasswordKey(email: String) {
        val normalizedEmail = email.normalizedEmail()
        val resetPasswordKey = randomString(6)
        val user = userRepository.updateResetPasswordKey(normalizedEmail, resetPasswordKey) ?: return
        mailService.sendResetPasswordKey(user.email, user.firstName, user.lastName, resetPasswordKey)
    }

    suspend fun resetPassword(reset: ResetPasswordDto): TokenDto {
        val normalizedEmail = reset.email.normalizedEmail()
        val user = userRepository.updatePasswordAndActivateWithKey(
            normalizedEmail,
            reset.verificationKey,
            passwordEncoder.encode(reset.newPassword),
        ) ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return createToken(user._id, user.roles.toList())
    }
}