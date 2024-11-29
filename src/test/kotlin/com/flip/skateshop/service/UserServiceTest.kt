package com.flip.skateshop.service

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.mapper.UserMapper
import com.flip.skateshop.repository.UserRepositoryWrapper
import com.flip.skateshop.security.JwtClaimer
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import com.flip.skateshop.web.rest.dto.ResetPasswordDto
import io.mockk.mockk
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserServiceTest
    @Autowired
    constructor(
        private val userRepository: UserRepositoryWrapper,
        private val jwtDecoder: ReactiveJwtDecoder,
        private val jwtClaimer: JwtClaimer,
        private val passwordEncoder: PasswordEncoder,
        private val skateshopProperties: SkateshopProperties,
        userMapper: UserMapper,
        authenticationManager: ReactiveAuthenticationManager,
    ) : ServicesCleaner() {
        private val userService =
            UserService(
                userRepository,
                userMapper,
                authenticationManager,
                jwtClaimer,
                mockk<SecurityUtils>(),
                mockk<MailService>(relaxed = true),
                passwordEncoder,
            )

        suspend fun createUser(
            id: ObjectId = ObjectId(),
            firstName: String = "Firstname",
            lastName: String = "LASTNAME",
            email: String = "test@test.fr",
            password: String = "password",
            roles: Set<RoleEnum> = emptySet(),
            activationKey: VerificationKey? = null,
            resetPasswordKey: VerificationKey? = null,
            enabled: Boolean = true,
        ) = userRepository.repository.save(
            User(
                ObjectId(),
                firstName,
                lastName,
                email,
                passwordEncoder.encode(password),
                roles,
                activationKey,
                resetPasswordKey,
                enabled,
            ),
        )

        @Test
        fun `should create a valid token`() =
            runTest {
                val userId = ObjectId()
                val roles = listOf(RoleEnum.ADMIN)
                val tokenDto = userService.createToken(userId, roles)
                val decodedToken = jwtDecoder.decode(tokenDto.token).awaitSingle()
                assertEquals(roles, tokenDto.roles)
                assertEquals(userId.toHexString(), decodedToken.subject)
                assertEquals(
                    roles.map { it.toString() },
                    jwtClaimer.claimAuthorities(decodedToken),
                )
            }

        @Test
        fun `should register an user`() =
            runTest {
                val registerDto =
                    RegisterDto(
                        "Firstname",
                        "LASTNAME",
                        "test@test.fr",
                        "password",
                    )
                userService.register(registerDto)
                val user = userRepository.repository.findOneByEmail(registerDto.email)
                assertNotNull(user)
                registerDto.run {
                    assertEquals(email, user.email)
                    assertEquals(firstName, user.firstName)
                    assertEquals(lastName, user.lastName)
                    assert(passwordEncoder.matches(password, user.password))
                    assertEquals(emptySet(), user.roles)
                    assertEquals(false, user.enabled)
                    assertEquals(null, user.resetPasswordKey)
                    assertNotNull(user.activationKey)
                }
            }

        @Test
        fun `should not register an user if email is already used`() =
            runTest {
                val registerDto =
                    RegisterDto(
                        "Firstname",
                        "LASTNAME",
                        "test@test.fr",
                        "password",
                    )
                userService.register(registerDto)
                val exception =
                    assertThrows<ResponseStatusException> {
                        userService.register(registerDto)
                    }
                assertEquals(HttpStatus.CONFLICT, exception.statusCode)
            }

        @Test
        fun `should login when credentials are valid`() =
            runTest {
                val password = "password"
                val user =
                    createUser(
                        password = password,
                        enabled = true,
                    )
                userService.login(
                    LoginDto(
                        user.email.uppercase(),
                        password,
                    ),
                )
            }

        @Test
        fun `should not login when credentials are invalid`() =
            runTest {
                val password = "password"
                val user =
                    createUser(
                        password = password,
                        enabled = true,
                    )
                assertThrows<BadCredentialsException> {
                    userService.login(
                        LoginDto(
                            user.email,
                            "pass",
                        ),
                    )
                }
                assertThrows<BadCredentialsException> {
                    userService.login(
                        LoginDto(
                            "truc@truc.fr",
                            password,
                        ),
                    )
                }
            }

        @Test
        fun `should not login when account is not enabled`() =
            runTest {
                val password = "password"
                val user =
                    createUser(
                        password = password,
                        enabled = false,
                    )
                assertThrows<ResponseStatusException> {
                    userService.login(
                        LoginDto(
                            user.email,
                            password,
                        ),
                    )
                }.run { assertEquals(HttpStatus.FORBIDDEN, statusCode) }
            }

        @Test
        fun `should set activation key`() =
            runTest {
                val user =
                    createUser(
                        activationKey = null,
                        enabled = false,
                    )
                userService.sendActivationKey(user.email)
                userRepository.repository.findById(user._id)?.run {
                    assertNotNull(activationKey)
                }
            }

        @Test
        fun `should set reset password key`() =
            runTest {
                val user =
                    createUser(
                        resetPasswordKey = null,
                    )
                userService.sendResetPasswordKey(user.email)
                userRepository.repository.findById(user._id)?.run {
                    assertNotNull(resetPasswordKey)
                }
            }

        @Test
        fun `should activate account with valid activation key`() =
            runTest {
                val activationKey = "ABCDEF"
                val user =
                    createUser(
                        activationKey =
                            VerificationKey(
                                activationKey,
                                Instant.now().plusSeconds(skateshopProperties.security.verificationKey.validityInSeconds),
                            ),
                        enabled = false,
                    )
                userService.activate(user.email, activationKey)
                assert(userRepository.repository.findById(user._id)?.enabled ?: false)
            }

        @Test
        fun `should not activate account when activation key is invalid`() =
            runTest {
                val user =
                    createUser(
                        activationKey =
                            VerificationKey(
                                "ABCDEF",
                                Instant.now().plusSeconds(skateshopProperties.security.verificationKey.validityInSeconds),
                            ),
                        enabled = false,
                    )
                assertThrows<ResponseStatusException> {
                    userService.activate(user.email, "012345")
                }.run { assertEquals(HttpStatus.FORBIDDEN, statusCode) }
                assert(
                    userRepository.repository
                        .findById(user._id)
                        ?.enabled
                        ?.not() ?: false,
                )
            }

        @Test
        fun `should not activate account when activation key is expired`() =
            runTest {
                val activationKey = "ABCDEF"
                val user =
                    createUser(
                        activationKey =
                            VerificationKey(
                                activationKey,
                                Instant.now().minusSeconds(1),
                            ),
                        enabled = false,
                    )
                assertThrows<ResponseStatusException> {
                    userService.activate(user.email, activationKey)
                }.run { assertEquals(HttpStatus.FORBIDDEN, statusCode) }
                assert(
                    userRepository.repository
                        .findById(user._id)
                        ?.enabled
                        ?.not() ?: false,
                )
            }

        @Test
        fun `should reset password when verification key is valid`() =
            runTest {
                val verificationKey = "ABCDEF"
                val user =
                    createUser(
                        resetPasswordKey =
                            VerificationKey(
                                verificationKey,
                                Instant.now().plusSeconds(skateshopProperties.security.verificationKey.validityInSeconds),
                            ),
                    )
                val newPassword = "newPassword"
                userService.resetPassword(ResetPasswordDto(user.email, newPassword, verificationKey))
                val updatedUser = userRepository.repository.findById(user._id)
                assert(passwordEncoder.matches(newPassword, updatedUser?.password))
            }

        @Test
        fun `should not reset password when verification key is invalid`() =
            runTest {
                val user =
                    createUser(
                        resetPasswordKey =
                            VerificationKey(
                                "ABCDEF",
                                Instant.now().plusSeconds(skateshopProperties.security.verificationKey.validityInSeconds),
                            ),
                    )
                val newPassword = "newPassword"
                assertThrows<ResponseStatusException> {
                    userService.resetPassword(ResetPasswordDto(user.email, newPassword, "123456"))
                }.run { assertEquals(HttpStatus.FORBIDDEN, statusCode) }
                val updatedUser = userRepository.repository.findById(user._id)
                assert(!passwordEncoder.matches(newPassword, updatedUser?.password))
            }

        @Test
        fun `should not reset password when verification key is expired`() =
            runTest {
                val verificationKey = "ABCDEF"
                val user =
                    createUser(
                        resetPasswordKey =
                            VerificationKey(
                                verificationKey,
                                Instant.now().minusSeconds(1),
                            ),
                    )
                val newPassword = "newPassword"
                assertThrows<ResponseStatusException> {
                    userService.resetPassword(ResetPasswordDto(user.email, newPassword, verificationKey))
                }.run { assertEquals(HttpStatus.FORBIDDEN, statusCode) }
                val updatedUser = userRepository.repository.findById(user._id)
                assert(!passwordEncoder.matches(newPassword, updatedUser?.password))
            }
    }
