package com.flip.skateshop.interfaces.service

import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.web.rest.dto.CommandDto
import com.flip.skateshop.web.rest.dto.CommandPageDto
import com.flip.skateshop.web.rest.dto.CommandsStatsDto
import com.flip.skateshop.web.rest.dto.ShortCommandDto
import org.bson.types.ObjectId

interface CommandServiceInterface {
    suspend fun initCommandForCurrentUser(): String

    suspend fun finalizeCommandForCurrentUser(sessionId: String): String

    suspend fun listCommandsForCurrentUser(): List<ShortCommandDto>

    suspend fun listCommandsForUser(userId: ObjectId): List<ShortCommandDto>

    suspend fun getCommandForCurrentUser(id: ObjectId): CommandDto

    suspend fun getCommand(id: ObjectId): CommandDto

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

    suspend fun getCommandsStats(): CommandsStatsDto
}
