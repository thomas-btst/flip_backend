package com.flip.skateshop.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("skateshop")
class SkateshopProperties(
    val client: Client,
    val logo: String,
    val security: Security,
    val minio: Minio,
    val stripe: Stripe,
) {
    class Client(
        val url: String,
    )

    class Security(
        val jwt: Jwt,
        val refreshToken: VerificationKey,
        val verificationKey: VerificationKey,
    ) {
        class Jwt(
            val secretKey: String,
            val tokenValidityInSeconds: Long,
        )

        class VerificationKey(
            val validityInSeconds: Long,
        )
    }

    class Minio(
        val accessKey: String,
        val bucket: String,
        val endpoint: String,
        val secretKey: String,
    )

    class Stripe(
        val privateKey: String,
    )
}
