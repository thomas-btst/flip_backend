package com.flip.skateshop.controller

import com.flip.skateshop.domain.Feedback
import com.flip.skateshop.domain.FeedbackId
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.FeedbackRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.CreateFeedbackDto
import com.flip.skateshop.web.rest.dto.LoginDto
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

@AutoConfigureMockMvc
class FeedbackControllerTest(
    @Autowired
    val webTestClient: WebTestClient,
    @Autowired
    val feedbackRepository: FeedbackRepositoryInterface,
    @Autowired
    val userService: UserServiceInterface,
    @Autowired
    val userRepository: UserRepositoryInterface,
) : ServicesCleaner() {
    fun initFeedback(
        _id: FeedbackId = FeedbackId(ObjectId(), ObjectId()),
        rate: Double = 4.6,
        comment: String = "commentary",
        date: Instant = Instant.now(),
    ) = Feedback(
        _id,
        rate,
        comment,
        date,
    )

    @Test
    fun `should retrieve a product successfully`() =
        runTest {
            val count = 8
            val productId = ObjectId()
            for (i in 0..<count) {
                feedbackRepository.save(initFeedback(_id = FeedbackId(ObjectId(), productId)))
            }
            webTestClient
                .get()
                .uri("/feedbacks/products/$productId")
                .exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should retrieve product with no feedback successfully`() =
        runTest {
            val productId = ObjectId()
            webTestClient
                .get()
                .uri("/feedbacks/products/$productId")
                .exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should not send product feedback without be authenticated`() =
        runTest {
            val productId = ObjectId()
            webTestClient
                .put()
                .uri("/feedbacks/products/$productId")
                .bodyValue(CreateFeedbackDto(3.0, "fkj"))
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

    @Test
    fun `should not send product feedback if rate is too big`() =
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
            val productId = ObjectId()
            webTestClient
                .put()
                .uri("/feedbacks/products/$productId")
                .bodyValue(CreateFeedbackDto(6.5, "fkj"))
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isBadRequest
        }

    @Test
    fun `should send product feedback successfully`() =
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
            val productId = ObjectId()
            webTestClient
                .put()
                .uri("/feedbacks/products/$productId")
                .bodyValue(CreateFeedbackDto(3.0, "fkj"))
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should not delete product feedback if not authenticated`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val userId = ObjectId()
            userRepository.save(
                User(
                    userId,
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
            val productId = ObjectId()
            val feedback = initFeedback(_id = FeedbackId(userId, productId))
            feedbackRepository.save(feedback)
            webTestClient
                .delete()
                .uri("/feedbacks/products/$productId")
                .exchange()
                .expectStatus()
                .isUnauthorized
        }
}
