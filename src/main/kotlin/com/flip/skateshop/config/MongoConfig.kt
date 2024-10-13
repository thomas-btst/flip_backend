package com.flip.skateshop.config

import com.flip.skateshop.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index

@Configuration
class MongoConfig(
    private val scope: CoroutineScope,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun initIndicesAfterStartup() {
        scope.launch {
            initIndices()
        }
    }

    private suspend fun initIndices() {
        mongoTemplate.indexOps(User::class.java)
            .ensureIndex(Index(User::email.name, Sort.Direction.ASC).unique()).awaitSingle()
    }
}