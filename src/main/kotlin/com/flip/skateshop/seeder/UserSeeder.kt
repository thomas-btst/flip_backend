package com.flip.skateshop.seeder

import com.flip.skateshop.config.Seeder
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class UserSeeder(
    private val userRepository: UserRepositoryInterface,
) : Seeder("users") {
    companion object {
        const val ADMIN_EMAIL = "admin@flip.fr"
    }

    override fun seed() {
        for (i in 1..5) {
            runBlocking {
                userRepository.save(
                    User(
                        ObjectId(),
                        faker.name().firstName(),
                        faker.name().lastName(),
                        faker.internet().emailAddress(),
                        null,
                        null,
                        "{noop}${faker.internet().password()}",
                        emptySet(),
                        null,
                        null,
                        null,
                        faker.bool().bool(),
                        emptyMap(),
                        emptyMap(),
                    ),
                )
            }
        }
    }
}
