package com.flip.skateshop.controller

import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.AddressDto
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.UpdateUserDto
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@AutoConfigureMockMvc
class UserControllerTest(
    @Autowired
    private val webTestClient: WebTestClient,
    @Autowired
    private val userService: UserServiceInterface,
    @Autowired
    private val userRepository: UserRepositoryInterface,
) : ServicesCleaner() {
    @Test
    fun `should retrieve user profile successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val address = Address("line1", "line2", "zipCode", "City")
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        address,
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        emptyMap(),
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users/current")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(user._id.toString())
                .jsonPath("$.firstName")
                .isEqualTo(user.firstName)
                .jsonPath("$.lastName")
                .isEqualTo(user.lastName)
                .jsonPath("$.email")
                .isEqualTo(user.email)
                .jsonPath("$.phone")
                .isEqualTo("")
                .jsonPath("$.address.line1")
                .isEqualTo(address.line1)
                .jsonPath("$.address.line2")
                .isEqualTo(address.line2)
                .jsonPath("$.address.zipCode")
                .isEqualTo(address.zipCode)
                .jsonPath("$.address.city")
                .isEqualTo(address.city)
        }

    @Test
    fun `should update user profile successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val address = Address("line1", "line2", "zipCode", "City")
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    address,
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .put()
                .uri("/users")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(UpdateUserDto("Prénom", "Nom", "0789379083", AddressDto("Line1", "", "13000", "Test")))
                .exchange()
                .expectStatus()
                .isNoContent
        }

    @Test
    fun `should update user logo successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            val body =
                MultipartBodyBuilder()
                    .apply {
                        part("logo", ClassPathResource("seed/product/skate/7Px_bRvyZEggGCctwIRVN-transformed.png"))
                    }.build()
            webTestClient
                .patch()
                .uri("/users/logo")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isNoContent
        }

    @Test
    fun `should paginate users successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        null,
                        "{noop}$password",
                        setOf(RoleEnum.ADMIN),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        emptyMap(),
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users?limit=10&page=0")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.users[0].id")
                .isEqualTo(user._id.toString())
                .jsonPath("$.pages")
                .isEqualTo(1)
        }

    @Test
    fun `should not paginate users if user does not have role admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users?limit=10&page=0")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody()
        }

    @Test
    fun `should retrieve an user`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        null,
                        "{noop}$password",
                        setOf(RoleEnum.ADMIN),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        emptyMap(),
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users/${user._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(user._id.toString())
                .jsonPath("$.firstName")
                .isEqualTo(user.firstName)
                .jsonPath("$.lastName")
                .isEqualTo(user.lastName)
                .jsonPath("$.email")
                .isEqualTo(user.email)
                .jsonPath("$.phone")
                .isEqualTo("")
        }

    @Test
    fun `should not retrieve an user if he does not exist`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    setOf(RoleEnum.ADMIN),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users/${ObjectId()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNotFound
        }

    @Test
    fun `should not retrieve an user if user does not have role admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users/${ObjectId()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden
        }

    @Test
    fun `should retrieve users stats successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    setOf(RoleEnum.ADMIN),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users/stats")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.count")
                .isEqualTo(1)
        }

    @Test
    fun `should not retrieve users stats if user is not admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/users/stats")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden()
        }
}
