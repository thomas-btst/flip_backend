package com.flip.skateshop.mapper

import com.flip.skateshop.domain.Address
import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.domain.Product
import com.flip.skateshop.web.rest.dto.AddressDto
import com.flip.skateshop.web.rest.dto.CommandDto
import com.flip.skateshop.web.rest.dto.CommandProductDto
import com.flip.skateshop.web.rest.dto.ShortCommandDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CommandMapper(
    private val fileMapper: FileMapper,
    private val productMapper: ProductMapper,
) {
    fun toCommand(
        commandId: ObjectId,
        userId: ObjectId,
        cart: Map<ObjectId, Long>,
        products: List<Product>,
        invoice: String,
        address: Address,
        date: Instant,
        total: Long,
    ): Command =
        Command(
            commandId,
            userId,
            invoice,
            date,
            address,
            cart.filter { products.firstOrNull { product -> product._id == it.key } !== null },
            total,
            CommandStatus.PENDING,
        )

    fun toCommandDto(
        command: Command,
        products: List<Product>,
    ): CommandDto =
        command.run {
            CommandDto(
                _id.toHexString(),
                command.userId.toHexString(),
                fileMapper.toPublicPath(invoice),
                date,
                address.run {
                    AddressDto(line1, line2, zipCode, city)
                },
                command.products.map { (cartProduct, quantity) ->
                    val product = products.firstOrNull { it._id == cartProduct }
                    CommandProductDto(cartProduct.toHexString(), product?.let(productMapper::toProductDto), quantity)
                },
                total,
                status,
            )
        }

    fun toShortCommandDto(command: Command): ShortCommandDto =
        command.run {
            ShortCommandDto(
                _id.toHexString(),
                fileMapper.toPublicPath(invoice),
                date,
                status,
                command.total,
            )
        }
}
