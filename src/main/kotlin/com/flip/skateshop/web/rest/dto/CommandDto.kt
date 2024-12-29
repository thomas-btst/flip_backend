package com.flip.skateshop.web.rest.dto

import com.flip.skateshop.domain.CommandStatus
import java.time.Instant

class CommandDto(
    val id: String,
    val userId: String,
    val invoice: String?,
    val date: Instant,
    val address: AddressDto,
    val products: List<CommandProductDto>,
    val total: Long,
    val status: CommandStatus?,
)

class ShortCommandDto(
    val id: String,
    val invoice: String?,
    val date: Instant,
    val status: CommandStatus?,
    val total: Long,
)

class CommandProductDto(
    val productId: String,
    val product: ProductDto?,
    val quantity: Long,
)

class SessionDto(
    val sessionId: String,
)

class CommandPageDto(
    val commands: List<ShortCommandDto>,
    val pages: Long,
)

class CommandStatusDto(
    val status: CommandStatus,
)
