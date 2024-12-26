package com.flip.skateshop.service

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.interfaces.service.CommandServiceInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.interfaces.service.MailServiceInterface
import com.flip.skateshop.interfaces.service.UserServiceInterface
import com.flip.skateshop.mapper.CommandMapper
import com.flip.skateshop.security.SecurityUtils
import com.flip.skateshop.web.rest.dto.CommandDto
import com.flip.skateshop.web.rest.dto.CommandPageDto
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
import kotlin.math.round

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

    override suspend fun addCommandForCurrentUser(): ObjectId {
        val user = userService.getCurrentUser()
        val commandId = ObjectId()
        val date = Instant.now()

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
                    val unitPriceTTC = product.price.toDouble() / 100
                    val unitPriceHT = round((unitPriceTTC / 120) * 10000) / 100
                    val price = round(unitPriceTTC * quantity * 100) / 100
                    listOf(InvoiceItem(product.name, quantity, unitPriceHT, unitPriceTTC, price))
                }
            }
        if (items.isEmpty()) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cart is empty")
        }
        val total: Long =
            (
                items
                    .map { item ->
                        item.totalPrice
                    }.reduce { total, price -> total + price } * 100
            ).toLong()

        val invoice = generateInvoice(ObjectId(), user, user.address, items, date, total.toDouble() / 100)
        val invoiceKey =
            fileService.putCommandInvoice(
                user._id,
                commandId,
                "invoice.html",
                invoice.toByteArray(),
                MediaType.TEXT_HTML.toString(),
            )
        val command = commandMapper.toCommand(commandId, user._id, user.cart, products, invoiceKey, user.address, date, total)
        commandRepository.save(command)
        userRepository.clearCart(user._id)
        mailService.sendCommandConfirmation(user.email, user.firstName, user.lastName, command._id.toHexString(), invoice)
        return commandId
    }

    override suspend fun listCommandsForCurrentUser(): List<ShortCommandDto> {
        val currentUserId = securityUtils.getCurrentUserId()
        val commands = commandRepository.findAllByUserId(currentUserId)
        return commands.map(commandMapper::toShortCommandDto).toList().reversed()
    }

    override suspend fun getCommandByIdForCurrentUser(id: ObjectId): CommandDto {
        val currentUserId = securityUtils.getCurrentUserId()
        val command =
            commandRepository.findByIdAndUserId(id, currentUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command with id $id not found")
        val products = productRepository.findByIdIn(command.products.keys.toList())
        return commandMapper.toCommandDto(command, products.toList())
    }

    override suspend fun getCommandByIdForUser(id: ObjectId): CommandDto {
        val command =
            commandRepository.findById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command with id $id not found")
        val products = productRepository.findByIdIn(command.products.keys.toList())
        return commandMapper.toCommandDto(command, products.toList())
    }

    override suspend fun cancelCommandForCurrentUser(id: ObjectId) {
        val currentUserId = securityUtils.getCurrentUserId()
        val result = commandRepository.cancelByIdAndUserId(id, currentUserId, CommandStatus.PENDING)
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
        val result = commandRepository.updateCommandStatus(commandId, commandStatus)
        if (result.matchedCount < 1) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Command with id $commandId not found")
        }
    }
}

class InvoiceItem(
    val name: String,
    val quantity: Long,
    val unitPriceHT: Double,
    val unitPriceTTC: Double,
    val totalPrice: Double,
)
