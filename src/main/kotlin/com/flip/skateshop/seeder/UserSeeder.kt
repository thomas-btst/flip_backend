package com.flip.skateshop.seeder

import com.flip.skateshop.config.Seeder
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class UserSeeder(
    private val userRepository: UserRepository,
) : Seeder("users") {
    companion object {
        const val ADMIN_EMAIL = "admin@flip.fr"
    }

    fun seedAdmin() {
        runBlocking {
            if (userRepository.findOneByEmail(ADMIN_EMAIL) != null) {
                return@runBlocking
            }
            userRepository.save(
                User(
                    ObjectId(),
                    "admin",
                    "admin",
                    ADMIN_EMAIL,
                    "{noop}admin",
                    setOf(RoleEnum.ADMIN),
                    null,
                    null,
                    faker.bool().bool(),
                ),
            )
        }
    }

    fun seedUser() {
        runBlocking {
            userRepository.save(
                User(
                    ObjectId(),
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.internet().emailAddress(),
                    "{noop}${faker.internet().password()}",
                    emptySet(),
                    null,
                    null,
                    faker.bool().bool(),
                ),
            )
        }
    }

    override fun seed() {
        seedAdmin()
        for (i in 1..5) {
            seedUser()
        }
    }
}
