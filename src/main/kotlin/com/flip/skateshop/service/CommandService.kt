package com.flip.skateshop.service

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.CommandServiceInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.interfaces.service.MailServiceInterface
import com.flip.skateshop.interfaces.service.PaymentServiceInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.CommandMapper
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.web.rest.dto.CommandDto
import com.flip.skateshop.web.rest.dto.CommandPageDto
import com.flip.skateshop.web.rest.dto.CommandsStatsDto
import com.flip.skateshop.web.rest.dto.ShortCommandDto
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class CommandService(
    private val productRepository: ProductRepositoryInterface,
    private val userService: UserServiceInterface,
    private val commandRepository: CommandRepositoryInterface,
    private val commandMapper: CommandMapper,
    private val fileService: FileServiceInterface,
    private val templateEngine: TemplateEngine,
    private val properties: SkateshopProperties,
    private val securityUtils: SecurityUtils,
    private val userRepository: UserRepositoryInterface,
    private val mailService: MailServiceInterface,
    private val paymentService: PaymentServiceInterface,
) : CommandServiceInterface {
    companion object {
        const val INVOICE_RESOURCE_DIR = "invoice"
    }

    suspend fun generateInvoice(
        commandId: ObjectId,
        user: User,
        address: Address,
        items: List<InvoiceItem>,
        date: Instant,
        total: Double,
    ): String =
        templateEngine.process(
            "$INVOICE_RESOURCE_DIR/invoice.html",
            Context().apply {
                setVariable("invoiceId", commandId.toHexString())
                setVariable("date", LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                setVariable("firstname", user.firstName)
                setVariable("lastname", user.lastName)
                setVariable("address", address.line1)
                setVariable("city", address.city)
                setVariable("postalCode", address.zipCode)
                setVariable("logo", properties.logo)
                setVariable("items", items)
                setVariable("total", total)
            },
        )

    override suspend fun initCommandForCurrentUser(): String {
        val user = userService.getCurrentUser()
        if (user.address == null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Address must be added before adding a command")
        }
        if (user.cart.isEmpty()) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cart is empty")
        }

        val products = productRepository.findByIdIn(user.cart.keys).toList()
        val items =
            user.cart.flatMap { (productId, quantity) ->
                val product = products.firstOrNull { it._id == productId }
                if (product == null) {
                    emptyList()
                } else {
                    listOf(Pair(product, quantity))
                }
            }
        if (items.isEmpty()) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cart is empty")
        }

        val commandId = ObjectId()
        val date = Instant.now()
        val session = paymentService.createSession(items, user.email)
        val command =
            commandMapper.toUnpaidCommand(
                commandId,
                session.id,
                user._id,
                user.cart,
                products,
                user.address,
                date,
                session.amountTotal,
            )
        commandRepository.save(command)
        userRepository.clearCart(user._id)
        return session.rawJsonObject["client_secret"].asString
    }

    override suspend fun finalizeCommandForCurrentUser(sessionId: String): String {
        val user = userService.getCurrentUser()
        val command =
            commandRepository.findByPaymentIdAndUserId(sessionId, user._id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found")
        if (command !is Command.UnPaid) {
            throw ResponseStatusException(HttpStatus.GONE, "Command already finalized")
        }

        val session = paymentService.retrieveSession(sessionId)

        if (session.status != "complete") {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Payment session not completed")
        }

        val items =
            session.lineItems.data.map { line ->
                InvoiceItem(
                    line.price.productObject.name,
                    line.quantity,
                    (line.price.unitAmount * 100 / 120).toDouble() / 100,
                    line.price.unitAmount.toDouble() / 100,
                    line.amountTotal.toDouble() / 100,
                )
            }

        val invoice = generateInvoice(ObjectId(), user, command.address, items, command.date, session.amountTotal.toDouble() / 100)
        val invoiceKey =
            fileService.putCommandInvoice(
                user._id,
                command._id,
                "invoice.html",
                invoice.toByteArray(),
                MediaType.TEXT_HTML.toString(),
            )
        val paidCommand = commandMapper.toPaidCommand(command, invoiceKey)
        commandRepository.save(paidCommand)
        mailService.sendCommandConfirmation(user.email, user.firstName, user.lastName, command._id.toHexString(), invoice)
        return command._id.toHexString()
    }

    override suspend fun listCommandsForCurrentUser(): List<ShortCommandDto> {
        val currentUserId = securityUtils.getCurrentUserId()
        val commands = commandRepository.findAllByUserId(currentUserId)
        return commands.map(commandMapper::toShortCommandDto).toList().reversed()
    }

    override suspend fun listCommandsForUser(userId: ObjectId): List<ShortCommandDto> {
        val commands = commandRepository.findAllByUserId(userId)
        return commands.map(commandMapper::toShortCommandDto).toList().reversed()
    }

    override suspend fun getCommandForCurrentUser(id: ObjectId): CommandDto {
        val currentUserId = securityUtils.getCurrentUserId()
        val command =
            commandRepository.findByIdAndUserId(id, currentUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command with id $id not found")
        val products = productRepository.findByIdIn(command.products.keys.toList())
        return commandMapper.toCommandDto(command, products.toList())
    }

    override suspend fun getCommand(id: ObjectId): CommandDto {
        val command =
            commandRepository.findById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command with id $id not found")
        val products = productRepository.findByIdIn(command.products.keys.toList())
        return commandMapper.toCommandDto(command, products.toList())
    }

    override suspend fun cancelCommandForCurrentUser(id: ObjectId) {
        val currentUserId = securityUtils.getCurrentUserId()
        val result =
            commandRepository.updatePaidCommandStatusByIdAndUserIdAndStatus(
                id,
                currentUserId,
                CommandStatus.PENDING,
                CommandStatus.CANCELED,
            )
        if (result.matchedCount < 1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command not found or not pending")
        }
    }

    override suspend fun getCommandsByPage(
        limit: Int,
        page: Long,
        search: String,
        status: CommandStatus?,
    ): CommandPageDto {
        var objectId: ObjectId? = null
        try {
            objectId = ObjectId(search)
        } catch (_: Throwable) {
        }
        val commandPage = commandRepository.findByNameLikeAndByStatusAndByPage(limit, page, objectId, status)
        val pages =
            if (limit > 0) {
                (commandPage.second + limit - 1) / limit
            } else {
                0L
            }
        return CommandPageDto(commandPage.first.map(commandMapper::toShortCommandDto).toList(), pages)
    }

    override suspend fun updateCommandStatus(
        commandId: ObjectId,
        commandStatus: CommandStatus,
    ) {
        val result = commandRepository.updatePaidCommandStatusById(commandId, commandStatus)
        if (result.matchedCount < 1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command with id $commandId not found")
        }
    }

    override suspend fun getCommandsStats(): CommandsStatsDto {
        val stats = commandRepository.getStats()
        val statsByMonth = commandRepository.getStatsByMonth().toList()
        val statusStats = commandRepository.getStatusStats().toList()
        val topProducts = commandRepository.getTopProducts().toList()
        val products = productRepository.findByIdIn(topProducts.map { it._id }).toList()
        return commandMapper.toCommandsStatsDto(stats, statsByMonth, statusStats, topProducts, products)
    }
}

class InvoiceItem(
    val name: String,
    val quantity: Long,
    val unitPriceHT: Double,
    val unitPriceTTC: Double,
    val totalPrice: Double,
)
