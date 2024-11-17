package com.flip.skateshop.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("skateshop")
class SkateshopProperties(
    val security: Security,
    val minio: Minio,
) {
    class Security(
        val jwt: Jwt,
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
}