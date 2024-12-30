package com.flip.skateshop.service

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.domain.VerificationKey
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.interfaces.service.MailServiceInterface
import com.flip.skateshop.interfaces.service.PaymentServiceInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.CommandMapper
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.util.ServicesCleaner
import com.stripe.model.checkout.Session
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.thymeleaf.TemplateEngine
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CommandServiceTest(
    @Autowired
    private val productRepository: ProductRepositoryInterface,
    @Autowired
    private val commandRepository: CommandRepositoryInterface,
    @Autowired
    private val commandMapper: CommandMapper,
    @Autowired
    private val fileService: FileServiceInterface,
    @Autowired
    private val templateEngine: TemplateEngine,
    @Autowired
    private val properties: SkateshopProperties,
    @Autowired
    private val userRepository: UserRepositoryInterface,
) : ServicesCleaner() {
    private val mailService = mockk<MailServiceInterface>(relaxed = true)

    private val userService = mockk<UserServiceInterface>()

    private val securityUtils = mockk<SecurityUtils>()

    private val paymentService = mockk<PaymentServiceInterface>(relaxed = true)

    val commandService =
        CommandService(
            productRepository,
            userService,
            commandRepository,
            commandMapper,
            fileService,
            templateEngine,
            properties,
            securityUtils,
            userRepository,
            mailService,
            paymentService,
        )

    suspend fun createUser(
        id: ObjectId = ObjectId(),
        firstName: String = "Firstname",
        lastName: String = "LASTNAME",
        email: String = "test@test.fr",
        address: Address? =
            Address(
                "27 rue Flip",
                "",
                "13000",
                "Marseille",
            ),
        password: String = "password",
        roles: Set<RoleEnum> = emptySet(),
        activationKey: VerificationKey? = null,
        resetPasswordKey: VerificationKey? = null,
        enabled: Boolean = true,
        refreshTokens: Map<String, Instant> = emptyMap(),
        cart: Map<ObjectId, Long> = emptyMap(),
    ) = userRepository.save(
        User(
            id,
            firstName,
            lastName,
            email,
            "0786713311",
            address,
            password,
            roles,
            null,
            activationKey,
            resetPasswordKey,
            enabled,
            refreshTokens,
            cart,
        ),
    )

    suspend fun createProduct(
        name: String = "Name",
        description: String = "Description",
        price: Long = 8,
        picture: String = "/path/to/file",
    ): Product {
        val product =
            Product.Skate(
                ObjectId(),
                name,
                description,
                price,
                picture,
            )
        productRepository.save(product)
        return product
    }

    suspend fun createCommand(
        id: ObjectId = ObjectId(),
        paymentId: String = ObjectId().toHexString(),
        userId: ObjectId = ObjectId(),
        invoice: String = "/path/to/invoice",
        date: Instant = Instant.now(),
        address: Address =
            Address(
                "line1",
                "line2",
                "13000",
                "Marseille",
            ),
        products: Map<ObjectId, Long> = emptyMap(),
        total: Long = 8L,
        status: CommandStatus = CommandStatus.PENDING,
    ) = commandRepository.save(
        Command.Paid(
            id,
            paymentId,
            userId,
            invoice,
            status,
            date,
            address,
            products,
            total,
        ),
    )

    @Test
    fun `should init command correctly`() =
        runTest {
            val product = createProduct()
            val user =
                createUser(
                    cart = mapOf(Pair(product._id, 8L)),
                )
            coEvery { userService.getCurrentUser() } returns user
            commandService.initCommandForCurrentUser()
            val updateUser = userRepository.findById(user._id)
            assertNotNull(updateUser)
            assert(updateUser.cart.isEmpty())
        }

    @Test
    fun `should finalize command for current user correctly`() =
        runTest {
            val product1 = createProduct()
            val quantity1 = 8L
            val quantity2 = 1L
            val product2 = createProduct()
            val sessionId = "sessionId"
            val user =
                createUser(
                    address =
                        Address(
                            "27 rue Flip",
                            "",
                            "13000",
                            "Marseille",
                        ),
                )
            val command =
                Command.UnPaid(
                    ObjectId(),
                    sessionId,
                    user._id,
                    Instant.now(),
                    user.address!!,
                    mapOf(Pair(product1._id, quantity1), Pair(product2._id, quantity2)),
                    8L,
                )
            val session = mockk<Session>(relaxed = true)
            commandRepository.save(command)
            coEvery { userService.getCurrentUser() } returns user
            every { paymentService.retrieveSession(sessionId) } returns session
            every { session.status } returns "complete"
            val commandId = commandService.finalizeCommandForCurrentUser(sessionId)
            val updatedCommand = commandRepository.findById(ObjectId(commandId))
            assertNotNull(updatedCommand)
            updatedCommand.run {
                assertEquals(commandId, _id.toHexString())
                assertEquals(user._id, updatedCommand.userId)
                user.address?.run {
                    assertEquals(line1, address.line1)
                    assertEquals(line2, address.line2)
                    assertEquals(zipCode, address.zipCode)
                    assertEquals(city, address.city)
                }
                assert(this is Command.Paid)
                if (this is Command.Paid) {
                    assertEquals(CommandStatus.PENDING, status)
                }
            }
        }

    @Test
    fun `should not add command if cart is empty`() =
        runTest {
            val user =
                createUser(
                    address =
                        Address(
                            "27 rue Flip",
                            "",
                            "13000",
                            "Marseille",
                        ),
                    cart = emptyMap(),
                )
            coEvery { userService.getCurrentUser() } returns user
            assertThrows<ResponseStatusException> { commandService.initCommandForCurrentUser() }.let {
                assertEquals(HttpStatus.CONFLICT, it.statusCode)
            }
        }

    @Test
    fun `should not add command if address is not set`() =
        runTest {
            val user =
                createUser(
                    address = null,
                    cart = mapOf(Pair(ObjectId(), 1L)),
                )
            coEvery { userService.getCurrentUser() } returns user
            assertThrows<ResponseStatusException> { commandService.initCommandForCurrentUser() }.let {
                assertEquals(HttpStatus.CONFLICT, it.statusCode)
            }
        }

    @Test
    fun `should list commands for current user`() =
        runTest {
            val userId = ObjectId()
            val count = 10
            for (i in 1..count) {
                createCommand(userId = userId)
            }
            for (i in 1..20) {
                createCommand()
            }
            coEvery { securityUtils.getCurrentUserId() } returns userId
            val commands = commandService.listCommandsForCurrentUser()
            assertEquals(count, commands.size)
        }

    @Test
    fun `should retrieve a command by Id for current user`() =
        runTest {
            val userId = ObjectId()
            val command = createCommand(userId = userId)
            coEvery { securityUtils.getCurrentUserId() } returns userId
            val foundCommand = commandService.getCommandForCurrentUser(command._id)
            assertNotNull(foundCommand)
        }

    @Test
    fun `should throw not found exception if command not for current user`() =
        runTest {
            val command = createCommand()
            coEvery { securityUtils.getCurrentUserId() } returns ObjectId()
            assertThrows<ResponseStatusException> { commandService.getCommandForCurrentUser(command._id) }.let {
                assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
            }
        }

    @Test
    fun `should retrieve a command by Id`() =
        runTest {
            val command = createCommand()
            coEvery { securityUtils.getCurrentUserId() } returns ObjectId()
            val foundCommand = commandService.getCommand(command._id)
            assertNotNull(foundCommand)
        }

    @Test
    fun `should cancel a command for current user correctly`() =
        runTest {
            val userId = ObjectId()
            val command = createCommand(userId = userId, status = CommandStatus.PENDING)
            coEvery { securityUtils.getCurrentUserId() } returns userId
            commandService.cancelCommandForCurrentUser(command._id)
            val updatedCommand = commandRepository.findById(command._id)
            assertNotNull(updatedCommand)
            assert(updatedCommand is Command.Paid)
            if (updatedCommand is Command.Paid) {
                assertEquals(CommandStatus.CANCELED, updatedCommand.status)
            }
        }

    @Test
    fun `should not cancel a command not pending`() =
        runTest {
            val userId = ObjectId()
            val command = createCommand(userId = userId, status = CommandStatus.IN_TRANSIT)
            coEvery { securityUtils.getCurrentUserId() } returns userId
            assertThrows<ResponseStatusException> { commandService.cancelCommandForCurrentUser(command._id) }.let {
                assertEquals(HttpStatus.NOT_FOUND, it.statusCode)
            }
        }

    @Test
    fun `should update a command status correctly`() =
        runTest {
            val command = createCommand(status = CommandStatus.PENDING)
            coEvery { securityUtils.getCurrentUserId() } returns ObjectId()
            commandService.updateCommandStatus(command._id, CommandStatus.IN_TRANSIT)
            val updatedCommand = commandRepository.findById(command._id)
            assertNotNull(updatedCommand)
            assert(updatedCommand is Command.Paid)
            if (updatedCommand is Command.Paid) {
                assertEquals(CommandStatus.IN_TRANSIT, updatedCommand.status)
            }
        }

    @Test
    fun `should list commands for an user correctly`() =
        runTest {
            val command = createCommand(status = CommandStatus.PENDING)
            createCommand(status = CommandStatus.PENDING)
            val commands = commandService.listCommandsForUser(command.userId)
            assertEquals(1, commands.size)
            commands.first().apply {
                assertEquals(command._id.toHexString(), id)
                assertEquals(CommandStatus.PENDING, status)
                assertEquals(command.total, total)
            }
        }
}
