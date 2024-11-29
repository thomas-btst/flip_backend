package com.flip.skateshop.config

import com.flip.skateshop.annotation.SeederAnnotation
import com.github.javafaker.Faker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import kotlin.system.exitProcess

@Configuration
class SeederConfig(
    private val applicationContext: ApplicationContext,
) : ApplicationRunner {
    companion object {
        const val SEED_OPTION = "seed"
    }

    private val logger: Logger = LoggerFactory.getLogger(SeederConfig::class.java)

    override fun run(args: ApplicationArguments) {
        if (args.containsOption(SEED_OPTION)) {
            runSeeders(args.getOptionValues(SEED_OPTION))
            logger.info("Seed finished successfully.")
            SpringApplication.exit(applicationContext, { 0 })
            exitProcess(0)
        }
    }

    fun runSeeders(seeders: List<String>) {
        logger.info("Seed started :")
        applicationContext
            .getBeansWithAnnotation(
                SeederAnnotation::class.java,
            ).map { (_, bean: Any) ->
                if (bean is Seeder) {
                    bean
                } else {
                    throw IllegalStateException(
                        "Class annotated with @Seeder must implement Seeder: " + bean.javaClass.name,
                    )
                }
            }.filter { seeders.isEmpty() || seeders.contains(it.name) }
            .forEach { seeder ->
                logger.info("- ${seeder.name.replaceFirstChar { it.uppercase() }}")
                seeder.seed()
            }
    }
}

@SeederAnnotation
abstract class Seeder(
    val name: String,
) {
    protected val faker = Faker()

    abstract fun seed()
}
