package com.flip.skateshop.repository

import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.flip.skateshop.util.ServicesCleaner
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommandRepositoryTest(
    @Autowired
    private val commandRepository: CommandRepositoryInterface,
) : ServicesCleaner() {
    fun command(
        id: ObjectId = ObjectId(),
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
    ) = Command(
        id,
        userId,
        invoice,
        date,
        address,
        products,
        total,
        status,
    )

    @Test
    fun `should save and retrieve a command successfully`() =
        runTest {
            val command = command()
            commandRepository.save(command)
            val updatedCommand = commandRepository.findById(command._id)
            assertNotNull(updatedCommand)
            updatedCommand.run {
                assertEquals(_id, command._id)
                assertEquals(userId, command.userId)
                assertEquals(invoice, command.invoice)
                address.run {
                    assertEquals(line1, command.address.line1)
                    assertEquals(line2, command.address.line2)
                    assertEquals(zipCode, command.address.zipCode)
                    assertEquals(city, command.address.city)
                }
                assertEquals(products.size, command.products.size)
                products.forEach { (productId, quantity) -> assertEquals(quantity, command.products.get(productId)) }
                assertEquals(total, command.total)
                assertEquals(status, command.status)
            }
        }

    @Test
    fun `should find all commands for an user`() =
        runTest {
            val userId = ObjectId()
            val count = 10
            for (i in 1..count) {
                commandRepository.save(command(userId = userId))
            }
            for (i in 1..20) {
                commandRepository.save(command())
            }
            val commands = commandRepository.findAllByUserId(userId)
            assertEquals(count, commands.toList().size)
        }

    @Test
    fun `should retrieve a command by userId and commandId correctly`() =
        runTest {
            val command = command()
            commandRepository.save(command)
            assertNotNull(commandRepository.findByIdAndUserId(command._id, command.userId))
            assertNull(commandRepository.findByIdAndUserId(command._id, ObjectId()))
        }

    @Test
    fun `should cancel a command successfully`() =
        runTest {
            val command = command(status = CommandStatus.PENDING)
            commandRepository.save(command)
            commandRepository.cancelByIdAndUserId(command._id, command.userId, CommandStatus.PENDING)
            val foundCommand = commandRepository.findById(command._id)
            assertEquals(CommandStatus.CANCELED, foundCommand?.status)
        }

    @Test
    fun `should not cancel a not pending command`() =
        runTest {
            val command = command(status = CommandStatus.DELIVERED)
            commandRepository.save(command)
            commandRepository.cancelByIdAndUserId(command._id, command.userId, CommandStatus.PENDING)
            val foundCommand = commandRepository.findById(command._id)
            assertEquals(command.status, foundCommand?.status)
        }

    @Test
    fun `should update command status successfully`() =
        runTest {
            val command = command(status = CommandStatus.PENDING)
            commandRepository.save(command)
            val newStatus = CommandStatus.DELIVERED
            commandRepository.updateCommandStatus(command._id, newStatus)
            val foundCommand = commandRepository.findById(command._id)
            assertEquals(newStatus, foundCommand?.status)
        }

    @Test
    fun `should paginate commands correctly`() =
        runTest {
            val limit = 20
            val count = 30L
            for (i in 1..count) {
                commandRepository.save(command())
            }

            val firstPagination =
                commandRepository.findByNameLikeAndByStatusAndByPage(
                    limit,
                    0,
                    null,
                    null,
                )
            val secondPagination =
                commandRepository.findByNameLikeAndByStatusAndByPage(
                    limit,
                    1,
                    null,
                    null,
                )

            assertEquals(limit, firstPagination.first.toList().size)
            assertEquals(count.toInt() - limit, secondPagination.first.toList().size)
            assertEquals(count, firstPagination.second)
            assertEquals(count, secondPagination.second)
        }
}
