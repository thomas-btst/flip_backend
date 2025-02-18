package com.flip.skateshop

import com.flip.skateshop.annotation.SeederAnnotation
import com.flip.skateshop.config.Seeder
import com.flip.skateshop.config.SeederConfig
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.repository.UserRepositoryInterface
import com.flip.skateshop.seeder.AdminSeeder
import com.flip.skateshop.seeder.ProductSeeder
import com.flip.skateshop.seeder.UserSeeder
import com.flip.skateshop.util.ServicesCleaner
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SeederTest
    @Autowired
    constructor(
        private val userSeeder: UserSeeder,
        private val productSeeder: ProductSeeder,
        private val adminSeeder: AdminSeeder,
        private val userRepository: UserRepositoryInterface,
        private val productRepository: ProductRepositoryInterface,
    ) : ServicesCleaner() {
        private val applicationContext = mockk<ApplicationContext>(relaxed = true)

        @Test
        fun `should start all beans successfully`() {
            val seeder1 = mockk<Seeder>(relaxed = true)
            val seeder2 = mockk<Seeder>(relaxed = true)
            every { applicationContext.getBeansWithAnnotation(SeederAnnotation::class.java) } returns
                mapOf(
                    "seeder1" to seeder1,
                    "seeder2" to seeder2,
                )
            every { seeder1.name } returns "seederOne"
            every { seeder2.name } returns "seederTwo"

            val seedRunner = SeederConfig(applicationContext)
            seedRunner.runSeeders(emptyList())

            verify { seeder1.seed() }
            verify { seeder2.seed() }
        }

        @Test
        fun `should start only 1 bean successfully`() {
            val seeder1 = mockk<Seeder>(relaxed = true)
            val seeder2 = mockk<Seeder>(relaxed = true)

            every { applicationContext.getBeansWithAnnotation(SeederAnnotation::class.java) } returns
                mapOf(
                    "seeder1" to seeder1,
                    "seeder2" to seeder2,
                )

            every { seeder1.name } returns "seederOne"
            every { seeder2.name } returns "seederTwo"

            val seedRunner = SeederConfig(applicationContext)

            seedRunner.runSeeders(listOf(seeder1.name))

            verify { seeder1.seed() }
            verify(exactly = 0) { seeder2.seed() }
        }

        @Test
        fun `should seed users successfully`() =
            runTest {
                userSeeder.seed()
                assertEquals(userRepository.count(), 5)
            }

        @Test
        fun `should seed products successfully`() =
            runTest {
                productSeeder.seed()
                assertEquals(productRepository.count(), 200)
            }

        @Test
        fun `should seed admin successfully`() =
            runTest {
                adminSeeder.seed()
                assertNotNull(userRepository.findOneByEmail(UserSeeder.ADMIN_EMAIL))
                assertEquals(userRepository.count(), 1)
            }
    }
