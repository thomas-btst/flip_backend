package com.flip.skateshop.seeder

import com.flip.skateshop.config.Seeder
import com.flip.skateshop.domain.RoleEnum
import com.flip.skateshop.domain.User
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class AdminSeeder(
    private val userRepository: UserRepositoryInterface,
) : Seeder("admin") {
    companion object {
        const val ADMIN_EMAIL = "admin@flip.fr"
    }

    override fun seed() {
        runBlocking {
            if (userRepository.findOneByEmail(UserSeeder.ADMIN_EMAIL) != null) {
                return@runBlocking
            }
            userRepository.save(
                User(
                    ObjectId(),
                    "admin",
                    "admin",
                    UserSeeder.ADMIN_EMAIL,
                    null,
                    null,
                    "{noop}AdminPassword123!",
                    setOf(RoleEnum.ADMIN),
                    null,
                    null,
                    null,
                    true,
                    emptyMap(),
                    emptyMap(),
                ),
            )
        }
    }
}
