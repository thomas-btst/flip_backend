package com.flip.skateshop.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("skateshop")
class SkateshopProperties(
    val cors: Cors,
) {
    class Cors(
        val allowed: Allowed,
    ) {
        class Allowed(val origin: String)
    }
}