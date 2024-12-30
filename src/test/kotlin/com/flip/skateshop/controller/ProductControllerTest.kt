package com.flip.skateshop.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.LoginDto
import com.flip.skateshop.web.rest.dto.UpdateProductDto
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
class ProductControllerTest(
    @Autowired
    val webTestClient: WebTestClient,
    @Autowired
    val userRepository: UserRepositoryInterface,
    @Autowired
    val userService: UserServiceInterface,
    @Autowired
    val productRepository: ProductRepositoryInterface,
    @Autowired
    val objectMapper: ObjectMapper,
) : ServicesCleaner() {
    @Test
    fun `should retrieve a product successfully`() =
        runTest {
            val product =
                Product.Skate(
                    ObjectId(),
                    "Nom",
                    "Desc",
                    8L,
                    "/path/to/picture",
                )
            productRepository.save(product)
            webTestClient
                .get()
                .uri("/public/products/${product._id}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(product._id.toHexString())
                .jsonPath("$.name")
                .isEqualTo(product.name)
                .jsonPath("$.description")
                .isEqualTo(product.description)
                .jsonPath("$.price")
                .isEqualTo(product.price)
                .jsonPath("$.picture")
                .isNotEmpty()
        }

    @Test
    fun `should return 404 if a product does not exists`() =
        runTest {
            webTestClient
                .get()
                .uri("/public/products/${ObjectId().toHexString()}")
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody()
        }

    @Test
    fun `should paginate product`() =
        runTest {
            val product =
                Product.Skate(
                    ObjectId(),
                    "Nom",
                    "Desc",
                    8L,
                    "/path/to/picture",
                )
            productRepository.save(product)
            webTestClient
                .get()
                .uri("/public/products/limit/80")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.products[0].id")
                .isEqualTo(product._id.toHexString())
                .jsonPath("$.hasMore")
                .isEqualTo(false)
        }

    @Test
    fun `should get products by page`() =
        runTest {
            val product =
                Product.Skate(
                    ObjectId(),
                    "Nom",
                    "Desc",
                    8L,
                    "/path/to/picture",
                )
            productRepository.save(product)
            webTestClient
                .get()
                .uri("/public/products/limit/80/page/0")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.products[0].id")
                .isEqualTo(product._id.toHexString())
                .jsonPath("$.pages")
                .isEqualTo(1)
        }

    @Test
    fun `should add product successfully`() =
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
            val body =
                MultipartBodyBuilder()
                    .apply {
                        part("picture", ClassPathResource("seed/product/skate/7Px_bRvyZEggGCctwIRVN-transformed.png"))
                        part(
                            "productDto",
                            objectMapper.writeValueAsString(CreateProductDto.Skate("Nom", "Desc", 8L)),
                            MediaType.APPLICATION_JSON,
                        )
                    }.build()
            webTestClient
                .post()
                .uri("/products")
                .header("Authorization", "Bearer ${token.accessToken}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody()
        }

    @Test
    fun `should not add a product and return 403 if not admin`() =
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
                        part("picture", ClassPathResource("seed/product/skate/7Px_bRvyZEggGCctwIRVN-transformed.png"))
                        part(
                            "productDto",
                            objectMapper.writeValueAsString(CreateProductDto.Skate("Nom", "Desc", 8L)),
                            MediaType.APPLICATION_JSON,
                        )
                    }.build()
            webTestClient
                .post()
                .uri("/products")
                .header("Authorization", "Bearer ${token.accessToken}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody()
        }

    @Test
    fun `should update a product successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = Product.Skate(ObjectId(), "", "", 8L, "")
            productRepository.save(product)
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
                .put()
                .uri("/products/${product._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(UpdateProductDto.Skate("Nom", "Desc", 8L))
                .exchange()
                .expectStatus()
                .isNoContent
                .expectBody()
        }

    @Test
    fun `should not update and return 404 if product does not exist`() =
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
                .put()
                .uri("/products/${ObjectId()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(UpdateProductDto.Skate("Nom", "Desc", 8L))
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody()
        }

    @Test
    fun `should not update and return 403 if user does not have role admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = Product.Skate(ObjectId(), "", "", 8L, "")
            userRepository.save(
                User(
                    ObjectId(),
                    "Thomas",
                    "BATISTA",
                    email,
                    "",
                    null,
                    "{noop}$password",
                    setOf(),
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
                .uri("/products/${product._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(UpdateProductDto.Skate("Nom", "Desc", 8L))
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody()
        }

    @Test
    fun `should delete a product successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = Product.Skate(ObjectId(), "", "", 8L, "/path/to/file")
            productRepository.save(product)
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
                .delete()
                .uri("/products/${product._id.toHexString()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNoContent
        }

    @Test
    fun `should not delete a product and return 404 if product does not exists`() =
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
                .delete()
                .uri("/products/${ObjectId().toHexString()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody()
        }

    @Test
    fun `should not delete a product and return 403 if user does not have role admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = Product.Skate(ObjectId(), "", "", 8L, "")
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
                .delete()
                .uri("/products/${product._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody()
        }

    @Test
    fun `should update a product picture successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = Product.Skate(ObjectId(), "", "", 8L, "/path/to/file")
            productRepository.save(product)
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
            val body =
                MultipartBodyBuilder()
                    .apply {
                        part("picture", ClassPathResource("seed/product/skate/7Px_bRvyZEggGCctwIRVN-transformed.png"))
                    }.build()
            webTestClient
                .patch()
                .uri("/products/${product._id}/picture")
                .header("Authorization", "Bearer ${token.accessToken}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isNoContent
                .expectBody()
        }

    @Test
    fun `should not update a product picture and return 404 if product does not exists`() =
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
            val body =
                MultipartBodyBuilder()
                    .apply {
                        part("picture", ClassPathResource("seed/product/skate/7Px_bRvyZEggGCctwIRVN-transformed.png"))
                    }.build()
            webTestClient
                .patch()
                .uri("/products/${ObjectId()}/picture")
                .header("Authorization", "Bearer ${token.accessToken}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody()
        }

    @Test
    fun `should not update a product picture and return 404 if user does not have role admin`() =
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
                        part("picture", ClassPathResource("seed/product/skate/7Px_bRvyZEggGCctwIRVN-transformed.png"))
                    }.build()
            webTestClient
                .patch()
                .uri("/products/${ObjectId()}/picture")
                .header("Authorization", "Bearer ${token.accessToken}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(body)
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody()
        }
}
