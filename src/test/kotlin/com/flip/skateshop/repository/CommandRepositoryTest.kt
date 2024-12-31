package com.flip.skateshop.repository

import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.flip.skateshop.util.ServicesCleaner
import com.mongodb.internal.operation.retry.AttachmentKeys.command
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommandRepositoryTest(
    @Autowired
    private val commandRepository: CommandRepositoryInterface,
) : ServicesCleaner() {
    fun paidCommand(
        id: ObjectId = ObjectId(),
        invoiceId: String = ObjectId().toHexString(),
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
    ) = Command.Paid(
        id,
        invoiceId,
        userId,
        invoice,
        status,
        date,
        address,
        products,
        total,
    )

    @Test
    fun `should save and retrieve a command successfully`() =
        runTest {
            val command = paidCommand()
            commandRepository.save(command)
            val updatedCommand = commandRepository.findById(command._id)
            assertNotNull(updatedCommand)
            updatedCommand.run {
                assert(this is Command.Paid)
                assertEquals(_id, command._id)
                assertEquals(userId, command.userId)
                if (this is Command.Paid) {
                    assertEquals(invoice, command.invoice)
                }
                address.run {
                    assertEquals(line1, command.address.line1)
                    assertEquals(line2, command.address.line2)
                    assertEquals(zipCode, command.address.zipCode)
                    assertEquals(city, command.address.city)
                }
                assertEquals(products.size, command.products.size)
                products.forEach { (productId, quantity) -> assertEquals(quantity, command.products.get(productId)) }
                assertEquals(total, command.total)
                if (this is Command.Paid) {
                    assertEquals(status, command.status)
                }
            }
        }

    @Test
    fun `should find all commands for an user`() =
        runTest {
            val userId = ObjectId()
            val count = 10
            for (i in 1..count) {
                commandRepository.save(paidCommand(userId = userId))
            }
            for (i in 1..20) {
                commandRepository.save(paidCommand())
            }
            val commands = commandRepository.findAllByUserId(userId)
            assertEquals(count, commands.toList().size)
        }

    @Test
    fun `should retrieve a command by userId and commandId correctly`() =
        runTest {
            val command = paidCommand()
            commandRepository.save(command)
            assertNotNull(commandRepository.findByIdAndUserId(command._id, command.userId))
            assertNull(commandRepository.findByIdAndUserId(command._id, ObjectId()))
        }

    @Test
    fun `should cancel a command successfully`() =
        runTest {
            val command = paidCommand(status = CommandStatus.PENDING)
            commandRepository.save(command)
            commandRepository.updatePaidCommandStatusByIdAndUserIdAndStatus(
                command._id,
                command.userId,
                CommandStatus.PENDING,
                CommandStatus.CANCELED,
            )
            val foundCommand = commandRepository.findById(command._id)
            assert(foundCommand is Command.Paid)
            if (foundCommand is Command.Paid) {
                assertEquals(CommandStatus.CANCELED, foundCommand.status)
            }
        }

    @Test
    fun `should not cancel a not pending command`() =
        runTest {
            val command = paidCommand(status = CommandStatus.DELIVERED)
            commandRepository.save(command)
            commandRepository.updatePaidCommandStatusByIdAndUserIdAndStatus(
                command._id,
                command.userId,
                CommandStatus.PENDING,
                CommandStatus.CANCELED,
            )
            val foundCommand = commandRepository.findById(command._id)
            assert(foundCommand is Command.Paid)
            if (foundCommand is Command.Paid) {
                assertEquals(command.status, foundCommand.status)
            }
        }

    @Test
    fun `should not cancel unpaid command status`() =
        runTest {
            val command =
                Command.UnPaid(
                    ObjectId(),
                    "",
                    ObjectId(),
                    Instant.now(),
                    Address("", "", "", ""),
                    emptyMap(),
                    0L,
                )
            commandRepository.save(command)
            val result =
                commandRepository.updatePaidCommandStatusByIdAndUserIdAndStatus(
                    command._id,
                    command.userId,
                    CommandStatus.PENDING,
                    CommandStatus.CANCELED,
                )
            assertEquals(0, result.matchedCount)
        }

    @Test
    fun `should update command status successfully`() =
        runTest {
            val command = paidCommand(status = CommandStatus.PENDING)
            commandRepository.save(command)
            val newStatus = CommandStatus.DELIVERED
            commandRepository.updatePaidCommandStatusById(command._id, newStatus)
            val foundCommand = commandRepository.findById(command._id)
            assert(foundCommand is Command.Paid)
            if (foundCommand is Command.Paid) {
                assertEquals(newStatus, foundCommand.status)
            }
        }

    @Test
    fun `should not update unpaid command status`() =
        runTest {
            val command =
                Command.UnPaid(
                    ObjectId(),
                    "",
                    ObjectId(),
                    Instant.now(),
                    Address("", "", "", ""),
                    emptyMap(),
                    0L,
                )
            commandRepository.save(command)
            val result = commandRepository.updatePaidCommandStatusById(command._id, CommandStatus.CANCELED)
            assertEquals(0, result.matchedCount)
        }

    @Test
    fun `should paginate commands correctly`() =
        runTest {
            val limit = 20
            val count = 30L
            for (i in 1..count) {
                commandRepository.save(paidCommand())
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

    @Test
    fun `should retrieve global stats correctly`() =
        runTest {
            val count = 4L
            val price = 5L
            for (i in 1..count) {
                commandRepository.save(paidCommand(total = 5))
            }
            val stats = commandRepository.getStats()
            assertEquals(count, stats.count)
            assertEquals(price * count, stats.total)
        }

    @Test
    fun `should retrieve status stats correctly`() =
        runTest {
            val deliveredCount = 4L
            val cancelledCount = 2L
            for (i in 1..deliveredCount) {
                commandRepository.save(paidCommand(status = CommandStatus.DELIVERED))
            }
            for (i in 1..cancelledCount) {
                commandRepository.save(paidCommand(status = CommandStatus.CANCELED))
            }
            commandRepository.save(paidCommand(status = CommandStatus.PENDING))
            val stats = commandRepository.getStatusStats().toList()
            assertEquals(deliveredCount, stats.firstOrNull { it._id == CommandStatus.DELIVERED }?.count)
            assertEquals(cancelledCount, stats.firstOrNull { it._id == CommandStatus.CANCELED }?.count)
            assertEquals(1L, stats.firstOrNull { it._id == CommandStatus.PENDING }?.count)
            assertNull(stats.firstOrNull { it._id == CommandStatus.IN_TRANSIT }?.count)
        }

    @Test
    fun `should retrieve top products successfully`() =
        runTest {
            val product1 = ObjectId()
            val product2 = ObjectId()
            val product3 = ObjectId()
            val count1 = 8L
            val count2 = 2L
            val count3 = 3L

            for (i in 1..count1) {
                commandRepository.save(
                    paidCommand(
                        products = mapOf(Pair(product1, 1L)),
                    ),
                )
            }

            for (i in 1..count2) {
                commandRepository.save(
                    paidCommand(
                        products = mapOf(Pair(product2, 1L)),
                    ),
                )
            }

            for (i in 1..count3) {
                commandRepository.save(
                    paidCommand(
                        products = mapOf(Pair(product3, 1L)),
                    ),
                )
            }

            commandRepository.save(paidCommand(products = mapOf(Pair(ObjectId(), 1L))))
            val topProducts = commandRepository.getTopProducts().toList()
            assertEquals(3, topProducts.size)
            assertEquals(count1, topProducts.firstOrNull { it._id == product1 }?.count)
            assertEquals(count2, topProducts.firstOrNull { it._id == product2 }?.count)
            assertEquals(count3, topProducts.firstOrNull { it._id == product3 }?.count)
        }

    @Test
    fun `should retrieve months stats correctly`() =
        runTest {
            val yearMonth1 = YearMonth.of(2024, 11)
            val yearMonth2 = YearMonth.of(2024, 12)
            val date1 =
                yearMonth1
                    .atDay(1)
                    .atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            val date2 =
                yearMonth2
                    .atDay(1)
                    .atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            val count1 = 8L
            val count2 = 3L
            for (i in 1..count1) {
                commandRepository.save(paidCommand(date = date1))
            }
            for (i in 1..count2) {
                commandRepository.save(paidCommand(date = date2))
            }
            val stats = commandRepository.getStatsByMonth().toList()
            assertEquals(2, stats.size)
//            TODO()
//            assertEquals(count1, stats.firstOrNull { it._id.year == yearMonth1.year && it._id.month + 1 == yearMonth1.monthValue }?.count)
//            assertEquals(count2, stats.firstOrNull { it._id.year == yearMonth2.year && it._id.month + 1 == yearMonth2.monthValue }?.count)
        }
}
