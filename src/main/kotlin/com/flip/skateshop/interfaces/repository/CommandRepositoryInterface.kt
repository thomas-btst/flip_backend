package com.flip.skateshop.interfaces.repository

import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId

interface CommandRepositoryInterface {
    suspend fun save(command: Command): Command

    suspend fun findAllByUserId(userId: ObjectId): Flow<Command>

    suspend fun findById(_id: ObjectId): Command?

    suspend fun findByIdAndUserId(
        _id: ObjectId,
        userId: ObjectId,
    ): Command?

    suspend fun cancelByIdAndUserId(
        _id: ObjectId,
        userId: ObjectId,
        status: CommandStatus,
    ): UpdateResult

    suspend fun findByNameLikeAndByStatusAndByPage(
        limit: Int,
        page: Long,
        search: ObjectId?,
        status: CommandStatus?,
    ): Pair<Flow<Command>, Long>

    suspend fun updateCommandStatus(
        id: ObjectId,
        status: CommandStatus,
    ): UpdateResult
}
