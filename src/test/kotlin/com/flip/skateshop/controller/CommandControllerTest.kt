package com.flip.skateshop.controller

import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.CommandStatusDto
import com.flip.skateshop.web.rest.dto.LoginDto
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

@AutoConfigureMockMvc
class CommandControllerTest(
    @Autowired
    private val webTestClient: WebTestClient,
    @Autowired
    private val userService: UserServiceInterface,
    @Autowired
    private val userRepository: UserRepositoryInterface,
    @Autowired
    private val productRepository: ProductRepositoryInterface,
    @Autowired
    private val commandRepository: CommandRepositoryInterface,
) : ServicesCleaner() {
    @Test
    fun `should init a command successfully`() =
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
                    Address("", "", "", ""),
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    mapOf(Pair(product._id, 8L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .post()
                .uri("/commands/sessions")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody()
        }

    @Test
    fun `should not init a command and return 409 if address is not set`() =
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
                    mapOf(Pair(product._id, 8L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .post()
                .uri("/commands/sessions")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
        }

    @Test
    fun `should not init a command and return 409 if cart is empty`() =
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
                    Address("", "", "", ""),
                    "{noop}$password",
                    emptySet(),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    mapOf(Pair(ObjectId(), 8L)),
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .post()
                .uri("/commands/sessions")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
        }

    @Test
    fun `should list commands for current user successfully`() =
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
                        Address("", "", "", ""),
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
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        user._id,
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(ObjectId(), 10L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/current_user")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.[0].id")
                .isEqualTo(command._id.toHexString())
                .jsonPath("$.[0].status")
                .isEqualTo(CommandStatus.PENDING.toString())
                .jsonPath("$.[0].total")
                .isEqualTo(command.total)
        }

    @Test
    fun `should list commands for user successfully`() =
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
                        Address("", "", "", ""),
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
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        user._id,
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(ObjectId(), 10L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/users/${user._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.[0].id")
                .isEqualTo(command._id.toHexString())
                .jsonPath("$.[0].status")
                .isEqualTo(CommandStatus.PENDING.toString())
                .jsonPath("$.[0].total")
                .isEqualTo(command.total)
        }

    @Test
    fun `should not list commands for user if user does not have role admin`() =
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
                        Address("", "", "", ""),
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
            commandRepository.save(
                Command.Paid(
                    ObjectId(),
                    "paymentId",
                    user._id,
                    "/path/to/invoice",
                    CommandStatus.PENDING,
                    Instant.now(),
                    Address("line1", "line2", "zipCode", "city"),
                    mapOf(Pair(ObjectId(), 10L)),
                    20L,
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/users/${user._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden
        }

    @Test
    fun `should get a command successfully for user`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        setOf(RoleEnum.ADMIN),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        user._id,
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/admin/${command._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(command._id.toHexString())
                .jsonPath("$.userId")
                .isEqualTo(command.userId.toHexString())
                .jsonPath("$.address.line1")
                .isEqualTo(command.address.line1)
                .jsonPath("$.address.line2")
                .isEqualTo(command.address.line2)
                .jsonPath("$.address.zipCode")
                .isEqualTo(command.address.zipCode)
                .jsonPath("$.address.city")
                .isEqualTo(command.address.city)
                .jsonPath("$.total")
                .isEqualTo(command.total)
                .jsonPath("$.status")
                .isEqualTo(CommandStatus.PENDING.toString())
                .jsonPath("$.products[0].productId")
                .isEqualTo(productId.toHexString())
                .jsonPath("$.products[0].quantity")
                .isEqualTo(10L)
                .jsonPath("$.products[0].product")
                .isEmpty()
                .jsonPath("$.products[1].productId")
                .isEqualTo(product._id.toHexString())
                .jsonPath("$.products[1].quantity")
                .isEqualTo(8L)
                .jsonPath("$.products[1].product")
                .isNotEmpty()
        }

    @Test
    fun `should not get a command successfully for user and return 404 if user does not exist`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        setOf(RoleEnum.ADMIN),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            commandRepository.save(
                Command.Paid(
                    ObjectId(),
                    "paymentId",
                    user._id,
                    "/path/to/invoice",
                    CommandStatus.PENDING,
                    Instant.now(),
                    Address("line1", "line2", "zipCode", "city"),
                    mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                    20L,
                ),
            )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/admin/${ObjectId()}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNotFound
        }

    @Test
    fun `should not get a command successfully for user and return 403 if user does not have role admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        user._id,
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/admin/${command._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden
        }

    @Test
    fun `should get a command successfully for current user`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        user._id,
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/${command._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(command._id.toHexString())
                .jsonPath("$.userId")
                .isEqualTo(command.userId.toHexString())
                .jsonPath("$.address.line1")
                .isEqualTo(command.address.line1)
                .jsonPath("$.address.line2")
                .isEqualTo(command.address.line2)
                .jsonPath("$.address.zipCode")
                .isEqualTo(command.address.zipCode)
                .jsonPath("$.address.city")
                .isEqualTo(command.address.city)
                .jsonPath("$.total")
                .isEqualTo(command.total)
                .jsonPath("$.status")
                .isEqualTo(CommandStatus.PENDING.toString())
                .jsonPath("$.products[0].productId")
                .isEqualTo(productId.toHexString())
                .jsonPath("$.products[0].quantity")
                .isEqualTo(10L)
                .jsonPath("$.products[0].product")
                .isEmpty()
                .jsonPath("$.products[1].productId")
                .isEqualTo(product._id.toHexString())
                .jsonPath("$.products[1].quantity")
                .isEqualTo(8L)
                .jsonPath("$.products[1].product")
                .isNotEmpty()
        }

    @Test
    fun `should not get a command successfully for current user and return 404 if command is not from this user`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        ObjectId(),
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .get()
                .uri("/commands/${command._id}")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNotFound
        }

    @Test
    fun `should cancel a command for current user successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        user._id,
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .patch()
                .uri("/commands/${command._id}/cancel")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNoContent
        }

    @Test
    fun `should not cancel a command for current user and return 404 if command does not exist`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        ObjectId(),
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .patch()
                .uri("/commands/${command._id}/cancel")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isNotFound
        }

    @Test
    fun `should update a command status for an user successfully`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        setOf(RoleEnum.ADMIN),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        ObjectId(),
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .patch()
                .uri("/commands/${command._id}/status")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(CommandStatusDto(CommandStatus.CANCELED))
                .exchange()
                .expectStatus()
                .isNoContent()
        }

    @Test
    fun `should not update a command status for an user and return 404 if command does not exist`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        setOf(RoleEnum.ADMIN),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        ObjectId(),
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .patch()
                .uri("/commands/${ObjectId()}/status")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(CommandStatusDto(CommandStatus.CANCELED))
                .exchange()
                .expectStatus()
                .isNotFound()
        }

    @Test
    fun `should not update a command status for an user and return 403 if user is not admin`() =
        runTest {
            val email = "test@test.com"
            val password = "password"
            val product = productRepository.save(Product.Skate(ObjectId(), "Nom", "Desc", 8L, "/path/to/file"))
            val user =
                userRepository.save(
                    User(
                        ObjectId(),
                        "Thomas",
                        "BATISTA",
                        email,
                        "",
                        Address("", "", "", ""),
                        "{noop}$password",
                        emptySet(),
                        null,
                        null,
                        null,
                        true,
                        emptyMap(),
                        mapOf(Pair(product._id, 8L)),
                    ),
                )
            val productId = ObjectId()
            val command =
                commandRepository.save(
                    Command.Paid(
                        ObjectId(),
                        "paymentId",
                        ObjectId(),
                        "/path/to/invoice",
                        CommandStatus.PENDING,
                        Instant.now(),
                        Address("line1", "line2", "zipCode", "city"),
                        mapOf(Pair(productId, 10L), Pair(product._id, 8L)),
                        20L,
                    ),
                )
            val token = userService.login(LoginDto(email, password))
            webTestClient
                .patch()
                .uri("/commands/${command._id}/status")
                .header("Authorization", "Bearer ${token.accessToken}")
                .bodyValue(CommandStatusDto(CommandStatus.CANCELED))
                .exchange()
                .expectStatus()
                .isForbidden()
        }

    @Test
    fun `should retrieve commands stats successfully`() =
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
                .uri("/commands/stats")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isOk
        }

    @Test
    fun `should not retrieve commands stats if user is not admin`() =
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
                .uri("/commands/stats")
                .header("Authorization", "Bearer ${token.accessToken}")
                .exchange()
                .expectStatus()
                .isForbidden()
        }
}
