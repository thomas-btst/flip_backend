package com.flip.skateshop.repository

import com.flip.skateshop.domain.Command
import com.flip.skateshop.domain.CommandStatus
import com.flip.skateshop.interfaces.repository.CommandRepositoryInterface
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mapping.div
import org.springframework.data.mapping.toDotPath
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ObjectOperators
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

interface CommandCRUDRepository : CoroutineCrudRepository<Command, ObjectId> {
    suspend fun findAllByUserId(userId: ObjectId): Flow<Command>

    @Suppress("FunctionName")
    suspend fun findBy_idAndUserId(
        _id: ObjectId,
        userId: ObjectId,
    ): Command?

    suspend fun findByPaymentIdAndUserId(
        paymentId: String,
        userId: ObjectId,
    ): Command?
}

class CommandsStatsMonth(
    val _id: Id,
    val count: Long,
    val total: Long,
) {
    class Id(
        val year: Int,
        val month: Int,
    )
}

class CommandsStatusStats(
    val _id: CommandStatus,
    val count: Long,
)

class CommandsTopProducts(
    val _id: ObjectId,
    val count: Long,
)

class CommandsStats(
    val total: Long,
    val count: Long,
)

@Repository
class CommandRepository(
    private val repository: CommandCRUDRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) : CommandRepositoryInterface {
    companion object {
        val matchPaid = Aggregation.match(Criteria.where("_class").`is`(Command.Paid.CLASS_NAME))
    }

    override suspend fun save(command: Command) = repository.save(command)

    override suspend fun findByPaymentIdAndUserId(
        paymentId: String,
        userId: ObjectId,
    ): Command? = repository.findByPaymentIdAndUserId(paymentId, userId)

    override suspend fun findAllByUserId(userId: ObjectId) = repository.findAllByUserId(userId)

    override suspend fun findById(_id: ObjectId): Command? = repository.findById(_id)

    override suspend fun findByIdAndUserId(
        _id: ObjectId,
        userId: ObjectId,
    ) = repository.findBy_idAndUserId(_id, userId)

    override suspend fun updatePaidCommandStatusByIdAndUserIdAndStatus(
        _id: ObjectId,
        userId: ObjectId,
        oldStatus: CommandStatus,
        newStatus: CommandStatus,
    ): UpdateResult {
        val query =
            Query().apply {
                addCriteria(Criteria.where("_class").`is`(Command.Paid.CLASS_NAME))
                addCriteria(Command::_id isEqualTo _id)
                addCriteria(Command::userId isEqualTo userId)
                addCriteria(Command.Paid::status isEqualTo oldStatus)
            }
        val update =
            Update().apply {
                set(Command.Paid::status.name, newStatus)
            }
        return mongoTemplate.updateFirst(query, update, Command::class.java).awaitFirst()
    }

    override suspend fun findByNameLikeAndByStatusAndByPage(
        limit: Int,
        page: Long,
        search: ObjectId?,
        status: CommandStatus?,
    ): Pair<Flow<Command>, Long> {
        val query =
            Query().apply {
                if (search != null) {
                    addCriteria(Command::_id isEqualTo search)
                }
                if (status != null) {
                    addCriteria(Command.Paid::status isEqualTo status)
                }
            }
        val count = mongoTemplate.count(query, Command::class.java).awaitSingle()
        query.apply {
            skip(limit * page)
            limit(limit)
            with(Sort.by(Sort.Direction.DESC, "_id"))
        }
        val commands = mongoTemplate.find(query.skip(limit * page).limit(limit), Command::class.java).asFlow()
        return Pair(commands, count)
    }

    override suspend fun updatePaidCommandStatusById(
        id: ObjectId,
        status: CommandStatus,
    ): UpdateResult {
        val query =
            Query().apply {
                addCriteria(Criteria.where("_class").`is`(Command.Paid.CLASS_NAME))
                addCriteria(Command::_id isEqualTo id)
            }
        val update =
            Update().apply {
                set(Command.Paid::status.name, status)
            }
        return mongoTemplate.updateFirst(query, update, Command::class.java).awaitSingle()
    }

    override suspend fun getStats(): CommandsStats {
        val groupAll =
            Aggregation
                .group()
                .sum(Command::total.name)
                .`as`(CommandsStats::total.name)
                .count()
                .`as`(CommandsStats::count.name)
        val aggregation =
            Aggregation.newAggregation(
                matchPaid,
                groupAll,
            )
        return mongoTemplate.aggregate<CommandsStats>(aggregation, Command.DOCUMENT_NAME).awaitFirstOrDefault(CommandsStats(0L, 0L))
    }

    override suspend fun getStatusStats(): Flow<CommandsStatusStats> {
        val groupByStatus = Aggregation.group(Command.Paid::status.name).count().`as`(CommandsStatusStats::count.name)

        val aggregation =
            Aggregation.newAggregation(
                matchPaid,
                groupByStatus,
            )
        return mongoTemplate.aggregate<CommandsStatusStats>(aggregation, Command.DOCUMENT_NAME).asFlow()
    }

    override suspend fun getStatsByMonth(): Flow<CommandsStatsMonth> {
        val project =
            Aggregation
                .project()
                .andExpression("month(${Command::date.name})")
                .`as`(CommandsStatsMonth.Id::month.name)
                .andExpression("year(${Command::date.name})")
                .`as`(CommandsStatsMonth.Id::year.name)
                .and(Command::total.name)
                .`as`(CommandsStatsMonth::total.name)
        val groupByDate =
            Aggregation
                .group(CommandsStatsMonth.Id::year.name, CommandsStatsMonth.Id::month.name)
                .count()
                .`as`(CommandsStatsMonth::count.name)
                .sum(Command::total.name)
                .`as`(CommandsStatsMonth::total.name)
        val sort =
            Aggregation.sort(Sort.Direction.ASC, (CommandsStatsMonth::_id / CommandsStatsMonth.Id::year).toDotPath()).and(
                Sort.Direction.ASC,
                (
                    CommandsStatsMonth::_id /
                        CommandsStatsMonth.Id::month
                ).toDotPath(),
            )

        val aggregation =
            Aggregation.newAggregation(
                matchPaid,
                project,
                groupByDate,
                sort,
            )
        return mongoTemplate.aggregate<CommandsStatsMonth>(aggregation, Command.DOCUMENT_NAME).asFlow()
    }

    override suspend fun getTopProducts(): Flow<CommandsTopProducts> {
        val projectToArray =
            Aggregation
                .project()
                .and(
                    ObjectOperators
                        .valueOf(Command::products.name)
                        .toArray(),
                ).`as`("products")
        val unwindProducts = Aggregation.unwind("products")
        val groupByProductId = Aggregation.group("products.k").sum("products.v").`as`(CommandsTopProducts::count.name)
        val sortByCount = Aggregation.sort(Sort.Direction.DESC, CommandsTopProducts::count.name)
        val aggregation =
            Aggregation.newAggregation(
                matchPaid,
                projectToArray,
                unwindProducts,
                groupByProductId,
                sortByCount,
                Aggregation.limit(3),
            )
        return mongoTemplate.aggregate<CommandsTopProducts>(aggregation, Command.DOCUMENT_NAME).asFlow()
    }
}
