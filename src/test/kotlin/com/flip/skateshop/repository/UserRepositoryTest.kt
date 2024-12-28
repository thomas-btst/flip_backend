package com.flip.skateshop.repository

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.AddressDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import com.github.javafaker.Faker
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest(
    @Autowired
    private val userRepository: UserRepositoryInterface,
    @Autowired
    private val properties: SkateshopProperties,
) : ServicesCleaner() {
    private val faker = Faker()

    private fun user(
        firstName: String = "Firstname",
        lastName: String = "Lastname",
        email: String = "user@example.com",
        phone: String = "0600000000",
        address: Address? = null,
        activationKey: VerificationKey? = null,
        resetPasswordKey: VerificationKey? = null,
        password: String = "{noop}password",
        roles: Set<RoleEnum> = emptySet(),
        logo: String? = null,
        enabled: Boolean = true,
        refreshTokens: Map<String, Instant> = emptyMap(),
        cart: Map<ObjectId, Long> = emptyMap(),
    ): User =
        User(
            ObjectId(),
            firstName,
            lastName,
            email,
            phone,
            address,
            password,
            roles,
            logo,
            activationKey,
            resetPasswordKey,
            enabled,
            refreshTokens,
            cart,
        )

    @Test
    fun `should save an user successfully and find it by id`() =
        runTest {
            val user = user()
            userRepository.save(user)
            val searchedUser = userRepository.findById(user._id)
            assertNotNull(searchedUser)
            user.run {
                assertEquals(firstName, searchedUser.firstName)
                assertEquals(lastName, searchedUser.lastName)
                assertEquals(email, searchedUser.email)
                address?.run {
                    assertNotNull(searchedUser.address)
                    assertEquals(line1, searchedUser.address.line1)
                    assertEquals(line2, searchedUser.address.line2)
                    assertEquals(zipCode, searchedUser.address.zipCode)
                    assertEquals(city, searchedUser.address.city)
                }
            }
        }

    @Test
    fun `should count users correctly`() =
        runTest {
            val count = 1L
            for (i in 1..count) {
                userRepository.save(user(email = faker.internet().emailAddress()))
            }
            assertEquals(count, userRepository.count())
        }

    @Test
    fun `should find an user by email`() =
        runTest {
            val user = user()
            userRepository.save(user)
            val searchedUser = userRepository.findOneByEmail(user.email)
            assertNotNull(searchedUser)
            user.run {
                assertEquals(_id, searchedUser._id)
                assertEquals(firstName, searchedUser.firstName)
                assertEquals(lastName, searchedUser.lastName)
                assertEquals(email, searchedUser.email)
                address?.run {
                    assertNotNull(searchedUser.address)
                    assertEquals(line1, searchedUser.address.line1)
                    assertEquals(line2, searchedUser.address.line2)
                    assertEquals(zipCode, searchedUser.address.zipCode)
                    assertEquals(city, searchedUser.address.city)
                }
            }
        }

    @Test
    fun `should update reset password key`() =
        runTest {
            val user = user()
            val key = "KEY"
            userRepository.save(user)
            userRepository.updateResetPasswordKey(user.email, key)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNotNull(resetPasswordKey)
                assertEquals(key, resetPasswordKey.key)
                assert(resetPasswordKey.expiration.isAfter(Instant.now())) // Check if the key expiration is in the future
                // Check if the expiration date is not taking so much time
                assert(
                    resetPasswordKey.expiration.isBefore(
                        Instant.now().plusSeconds(properties.security.verificationKey.validityInSeconds),
                    ),
                )
            }
        }

    @Test
    fun `should change password successfully`() =
        runTest {
            val user =
                user(
                    password = "password",
                    resetPasswordKey =
                        VerificationKey(
                            "KEY",
                            Instant.now().plusSeconds(properties.security.verificationKey.validityInSeconds),
                        ),
                    enabled = false,
                )
            userRepository.save(user)
            val newPassword = "{noop}newPassword"
            userRepository.updatePasswordAndActivateWithKey(user.email, user.resetPasswordKey!!.key, newPassword)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNull(resetPasswordKey)
                assertEquals(newPassword, password)
                assert(enabled)
            }
        }

    @Test
    fun `should not change password if key is invalid`() =
        runTest {
            val user =
                user(
                    resetPasswordKey =
                        VerificationKey(
                            "KEY",
                            Instant.now().plusSeconds(properties.security.verificationKey.validityInSeconds),
                        ),
                    enabled = false,
                )
            userRepository.save(user)
            userRepository.updatePasswordAndActivateWithKey(user.email, "BAD", "{noop}newPassword")
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNotNull(resetPasswordKey)
                assertEquals(user.password, password)
                assert(!enabled)
            }
        }

    @Test
    fun `should not change password if key is expired`() =
        runTest {
            val user =
                user(
                    resetPasswordKey =
                        VerificationKey(
                            "KEY",
                            Instant.now().minusSeconds(1L),
                        ),
                    enabled = false,
                )
            userRepository.save(user)
            userRepository.updatePasswordAndActivateWithKey(user.email, user.resetPasswordKey!!.key, "{noop}newPassword")
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNotNull(resetPasswordKey)
                assertEquals(user.password, password)
                assert(!enabled)
            }
        }

    @Test
    fun `should update activation key`() =
        runTest {
            val user = user(enabled = false)
            val key = "KEY"
            userRepository.save(user)
            userRepository.updateActivationKey(user.email, key)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNotNull(activationKey)
                assertEquals(key, activationKey.key)
                assert(activationKey.expiration.isAfter(Instant.now())) // Check if the key expiration is in the future
                // Check if the expiration date is not taking so much time
                assert(
                    activationKey.expiration.isBefore(
                        Instant.now().plusSeconds(properties.security.verificationKey.validityInSeconds),
                    ),
                )
            }
        }

    @Test
    fun `should activate account successfully`() =
        runTest {
            val user =
                user(
                    activationKey =
                        VerificationKey(
                            "KEY",
                            Instant.now().plusSeconds(properties.security.verificationKey.validityInSeconds),
                        ),
                    enabled = false,
                )
            userRepository.save(user)
            userRepository.activateAccountWithKey(user.email, user.activationKey!!.key)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNull(activationKey)
                assert(enabled)
            }
        }

    @Test
    fun `should not activate account if key is invalid`() =
        runTest {
            val user =
                user(
                    activationKey =
                        VerificationKey(
                            "KEY",
                            Instant.now().plusSeconds(properties.security.verificationKey.validityInSeconds),
                        ),
                    enabled = false,
                )
            userRepository.save(user)
            userRepository.activateAccountWithKey(user.email, "BAD")
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNotNull(activationKey)
                assert(!enabled)
            }
        }

    @Test
    fun `should not activate account if key is expired`() =
        runTest {
            val user =
                user(
                    activationKey =
                        VerificationKey(
                            "KEY",
                            Instant.now().minusSeconds(1L),
                        ),
                    enabled = false,
                )
            userRepository.save(user)
            userRepository.activateAccountWithKey(user.email, user.activationKey!!.key)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updatedUser.run {
                assertNotNull(activationKey)
                assert(!enabled)
            }
        }

    @Test
    fun `should update user profile correctly`() =
        runTest {
            val user =
                user(
                    firstName = "Firstname",
                    lastName = "Lastname",
                    phone = "0789302874",
                    address = null,
                )
            userRepository.save(user)
            val updateUserDto =
                UpdateUserDto(
                    "New firstname",
                    "New lastname",
                    "0839279102",
                    address =
                        AddressDto(
                            "27 rue Flip",
                            "",
                            "13000",
                            "Marseille",
                        ),
                )
            userRepository.updateUserProfile(user._id, updateUserDto)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            updateUserDto.run {
                assertEquals(firstName, updatedUser.firstName)
                assertEquals(lastName, updatedUser.lastName)
                assertEquals(phone, updatedUser.phone)
                assertNotNull(updatedUser.address)
                address.run {
                    assertEquals(line1, updatedUser.address.line1)
                    assertEquals(line2, updatedUser.address.line2)
                    assertEquals(zipCode, updatedUser.address.zipCode)
                    assertEquals(city, updatedUser.address.city)
                }
            }
        }

    @Test
    fun `should update user logo correctly`() =
        runTest {
            val user = user(logo = null)
            userRepository.save(user)
            val logo = "/path/to/file"
            userRepository.updateUserLogo(user._id, logo)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertEquals(logo, updatedUser.logo)
        }

    @Test
    fun `should add a product to the cart successfully`() =
        runTest {
            val user = user(cart = emptyMap())
            userRepository.save(user)
            val productId = ObjectId()
            val quantity = 8L
            userRepository.addToCart(user._id, productId, quantity)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertEquals(updatedUser.cart[productId], quantity)
        }

    @Test
    fun `should delete a product to the cart correctly`() =
        runTest {
            val productId = ObjectId()
            val user = user(cart = mapOf(Pair(productId, 8L)))
            userRepository.save(user)
            userRepository.removeFromCart(user._id, productId)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertNull(updatedUser.cart[productId])
        }

    @Test
    fun `should clear cart successfully`() =
        runTest {
            val user =
                user(
                    cart =
                        mapOf(
                            Pair(ObjectId(), 8L),
                            Pair(ObjectId(), 2L),
                            Pair(ObjectId(), 8L),
                            Pair(ObjectId(), 1L),
                            Pair(ObjectId(), 6L),
                        ),
                )
            userRepository.save(user)
            userRepository.clearCart(user._id)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assert(updatedUser.cart.isEmpty())
        }

    @Test
    fun `should add refresh token correctly`() =
        runTest {
            val user = user(refreshTokens = emptyMap())
            userRepository.save(user)
            val token = "token"
            userRepository.addRefreshToken(user._id, token, Instant.now())
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertEquals(1, updatedUser.refreshTokens.size)
            assertNotNull(updatedUser.refreshTokens[token])
        }

    @Test
    fun `should delete a refresh token correctly`() =
        runTest {
            val tokenToDelete = "tokenToDelete"
            val user =
                user(
                    refreshTokens =
                        mapOf(
                            Pair(tokenToDelete, Instant.now()),
                            Pair("token", Instant.now()),
                        ),
                )
            userRepository.save(user)
            userRepository.deleteRefreshToken(user._id, tokenToDelete)
            val updatedUser = userRepository.findById(user._id)
            assertNotNull(updatedUser)
            assertEquals(1, updatedUser.refreshTokens.size)
            assertNull(updatedUser.refreshTokens[tokenToDelete])
        }

    @Test
    fun `should check if a refresh token is valid correctly`() =
        runTest {
            val token = "token"
            val user =
                user(
                    refreshTokens =
                        mapOf(
                            Pair(token, Instant.now().plusSeconds(properties.security.refreshToken.validityInSeconds)),
                        ),
                )
            userRepository.save(user)
            assertNotNull(userRepository.findByIdAndRefreshTokenExistsAndNotExpired(user._id, token))
        }

    @Test
    fun `should check if a refresh token is invalid`() =
        runTest {
            val user =
                user(
                    refreshTokens =
                        mapOf(
                            Pair("token", Instant.now().plusSeconds(properties.security.refreshToken.validityInSeconds)),
                        ),
                )
            userRepository.save(user)
            assertNull(userRepository.findByIdAndRefreshTokenExistsAndNotExpired(user._id, "bad token"))
        }

    @Test
    fun `should check if a refresh token is expired`() =
        runTest {
            val token = "token"
            val user =
                user(
                    refreshTokens =
                        mapOf(
                            Pair(token, Instant.now().minusSeconds(1L)),
                        ),
                )
            userRepository.save(user)
            assertNull(userRepository.findByIdAndRefreshTokenExistsAndNotExpired(user._id, token))
        }

    @Test
    fun `should find all users by page correctly`() =
        runTest {
            val limit = 20
            val count = 30L
            for (i in 1..count) {
                userRepository.save(user())
            }

            val firstPagination =
                userRepository.findByEmailLikeAndByPage(
                    limit,
                    0,
                    "",
                )
            val secondPagination =
                userRepository.findByEmailLikeAndByPage(
                    limit,
                    1,
                    "",
                )

            assertEquals(limit, firstPagination.first.toList().size)
            assertEquals(count.toInt() - limit, secondPagination.first.toList().size)
            assertEquals(count, firstPagination.second)
            assertEquals(count, secondPagination.second)
        }

    fun `should search by email successfully in pagination`() =
        runTest {
            userRepository.save(user(email = "email@email.com"))
            userRepository.save(user(email = "test@test.com"))

            val findAll =
                userRepository
                    .findByEmailLikeAndByPage(
                        5,
                        0,
                        "",
                    ).first

            val findEmailBegin =
                userRepository.findByEmailLikeAndByPage(
                    5,
                    0,
                    "email",
                )

            val findEmailEnd =
                userRepository.findByEmailLikeAndByPage(
                    5,
                    0,
                    "email.com",
                )

            val findEmailMiddle =
                userRepository.findByEmailLikeAndByPage(
                    5,
                    0,
                    "email.",
                )

            assertEquals(2, findAll.toList().size)
            assertEquals(1, findEmailBegin.toList().size)
            assertEquals(1, findEmailEnd.toList().size)
            assertEquals(1, findEmailMiddle.toList().size)
        }
}
