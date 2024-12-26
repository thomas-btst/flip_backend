package com.flip.skateshop.interfaces.service

import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.web.rest.dto.CommandDto
import com.flip.skateshop.web.rest.dto.CommandPageDto
import com.flip.skateshop.web.rest.dto.ShortCommandDto
import org.bson.types.ObjectId

interface CommandServiceInterface {
    suspend fun addCommandForCurrentUser(): ObjectId

    suspend fun listCommandsForCurrentUser(): List<ShortCommandDto>

    suspend fun getCommandByIdForCurrentUser(id: ObjectId): CommandDto

    suspend fun getCommandByIdForUser(id: ObjectId): CommandDto

    suspend fun cancelCommandForCurrentUser(id: ObjectId)

    suspend fun getCommandsByPage(
        limit: Int,
        page: Long,
        search: String,
        status: CommandStatus?,
    ): CommandPageDto

    suspend fun updateCommandStatus(
        commandId: ObjectId,
        commandStatus: CommandStatus,
    )
}
