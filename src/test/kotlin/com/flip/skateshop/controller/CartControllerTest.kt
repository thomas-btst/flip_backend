package com.flip.skateshop.controller

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.CartQuantityDto
import com.flip.skateshop.web.rest.dto.LoginDto
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureMockMvc
class CartControllerTest(
    @Autowired
    private val webTestClient: WebTestClient,
    @Autowired
    private val userRepository: UserRepositoryInterface,
    @Autowired
    private val userService: UserServiceInterface,
    @Autowired
    private val productRepository: ProductRepositoryInterface,
) : ServicesCleaner() {
    @Test
    fun `should retrieve the cart`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
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
                    mapOf(Pair(product._id, 3L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/carts")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.products[0].id")
                .isEqualTo(product._id.toHexString())
                .jsonPath("$.products[0].name")
                .isEqualTo(product.name)
                .jsonPath("$.products[0].price")
                .isEqualTo(product.price)
                .jsonPath("$.products[0].quantity")
                .isEqualTo(3L)
        }

    @Test
    fun `should add a product to cart correctly`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
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
                .patch()
                .uri("/carts/${product._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(CartQuantityDto(8L))
                .exchange()
                .expectStatus()
                .isNoContent
                .expectBody()
        }

    @Test
    fun `should not add a product to cart and return 404 if product does not exist`() =
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
                .patch()
                .uri("/carts/${ObjectId()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(CartQuantityDto(8L))
                .exchange()
                .expectStatus()
                .isNotFound
        }

    @Test
    fun `should delete a product from cart correctly`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val productId = ObjectId()
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
                    mapOf(Pair(productId, 3L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .delete()
                .uri("/carts/$productId")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNoContent
                .expectBody()
        }

    @Test
    fun `should not delete a product from cart that is not in the cart`() =
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
                    mapOf(),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .delete()
                .uri("/carts/${ObjectId()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody()
        }

    @Test
    fun `should clear the cart correctly`() =
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
                    mapOf(Pair(ObjectId(), 3L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .delete()
                .uri("/carts")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNoContent
                .expectBody()
        }

    @Test
    fun `should get product quantity in the cart correctly`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val productId = ObjectId()
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
                    mapOf(Pair(productId, 3L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/carts/$productId/quantity")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.quantity", 3L)

            webTestClient
                .get()
                .uri("/carts/${ObjectId()}/quantity")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.quantity", 0L)
        }
}
