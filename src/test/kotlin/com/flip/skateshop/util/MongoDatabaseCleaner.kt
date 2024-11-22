package com.flip.skateshop.util

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class MongoDatabaseCleaner {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @PostConstruct
    @AfterEach
    fun cleanDatabase() {
        runBlocking {
            mongoTemplate.collectionNames
                .collect { mongoTemplate.dropCollection(it).awaitSingleOrNull() }
        }
    }
}
