package com.flip.skateshop

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.config.SpringProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SkateshopProperties::class, SpringProperties::class)
class SkateshopApplication

fun main(args: Array<String>) {
    runApplication<SkateshopApplication>(*args)
}
