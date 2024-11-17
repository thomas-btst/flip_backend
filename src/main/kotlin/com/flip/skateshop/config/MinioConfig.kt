package com.flip.skateshop.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioAsyncClient
import io.minio.SetBucketPolicyArgs
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig {
    companion object {
        const val PUBLIC_ROOT = "public"
    }

    @Bean
    fun minioClient(properties: SkateshopProperties): MinioAsyncClient {
        return MinioAsyncClient.builder()
            .credentials(properties.minio.accessKey, properties.minio.secretKey)
            .endpoint(properties.minio.endpoint)
            .build()
            .also { client ->
                client.bucketExists(BucketExistsArgs.builder().bucket(properties.minio.bucket).build())
                    .thenAccept { exists ->
                        if (!exists) {
                            client.makeBucket(MakeBucketArgs.builder().bucket(properties.minio.bucket).build())
                                .thenRun {
                                    client.setBucketPolicy(
                                        SetBucketPolicyArgs.builder().apply {
                                            bucket(properties.minio.bucket)
                                            config(
                                                """
                                        {
                                            "Version": "2012-10-17",
                                            "Statement": [
                                                {
                                                    "Effect": "Allow",
                                                    "Principal": "*",
                                                    "Action": [
                                                        "s3:GetObject"
                                                    ],
                                                    "Resource": [
                                                        "arn:aws:s3:::${properties.minio.bucket}/$PUBLIC_ROOT/*"
                                                    ]
                                                }
                                            ]
                                        }
                                    """.trimIndent()
                                            )
                                        }.build()
                                    )
                                }
                        }
                    }
            }
    }
}
