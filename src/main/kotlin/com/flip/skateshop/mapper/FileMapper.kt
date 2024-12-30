package com.flip.skateshop.mapper

import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.service.FileService
import org.springframework.stereotype.Component

@Component
class FileMapper(
    private val fileService: FileService,
    skateShopProperties: SkateshopProperties,
) {
    private val properties = skateShopProperties.minio

    fun toPublicPath(key: String): String = "${properties.endpoint}/${properties.bucket}/$key"

    suspend fun toPrivatePath(key: String): String = fileService.signUrl(key)
}
