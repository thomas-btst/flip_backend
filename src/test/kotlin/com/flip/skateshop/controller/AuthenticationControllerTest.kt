package com.flip.skateshop.controller

import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.RefreshTokenDto
import com.flip.skateshop.web.rest.dto.RegisterDto
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.Test

@AutoConfigureMockMvc
class AuthenticationControllerTest(
    @Autowired
    val webTestClient: WebTestClient,
    @Autowired
    val userRepository: UserRepositoryInterface,
    @Autowired
    val userService: UserServiceInterface,
) : ServicesCleaner() {
    @Test
    fun `should register an user`() {
        webTestClient
            .post()
            .uri("/auth/register")
            .bodyValue(RegisterDto("Thomas", "BATISTA", "test@test.com", "Password123#"))
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `should login an user correctly`() =
        runTest {
            val email = "test@test.com"
            val password = "Password123#"
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
            webTestClient
                .post()
                .uri("/auth/login")
                .bodyValue(LoginDto(email, password))
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
        }

    @Test
    fun `should refresh a token correctly`() =
        runTest {
            val email = "test@test.com"
            val password = "Password123#"
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
                .post()
                .uri("/auth/token/refresh")
                .bodyValue(RefreshTokenDto(token.accessToken, token.refreshToken))
                .exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should logout correctly`() =
        runTest {
            val email = "test@test.com"
            val password = "Password123#"
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
                .post()
                .uri("/auth/logout")
                .bodyValue(RefreshTokenDto(token.accessToken, token.refreshToken))
                .exchange()
                .expectStatus()
                .isNoContent
        }
}
