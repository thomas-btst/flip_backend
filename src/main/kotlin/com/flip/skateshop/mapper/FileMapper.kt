package com.flip.skateshop.mapper

import com.flip.skateshop.config.SkateshopProperties
import org.springframework.stereotype.Component

@Component
class FileMapper(
    skateShopProperties: SkateshopProperties,
) {
    private val properties = skateShopProperties.minio

    fun toPublicPath(key: String): String = "${properties.endpoint}/${properties.bucket}/$key"
}
