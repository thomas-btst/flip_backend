package com.flip.skateshop.service

import com.flip.skateshop.config.MinioConfig.Companion.PUBLIC_ROOT
import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.extention.toByteArray
import io.minio.MinioAsyncClient
import io.minio.ObjectWriteResponse
import io.minio.PutObjectArgs
import kotlinx.coroutines.future.await
import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class FileService(
    private val minioClient: MinioAsyncClient,
    skateshopProperties: SkateshopProperties,
) {
    private val properties = skateshopProperties.minio

    companion object {
        const val PRODUCT_ROOT = "products"
        const val PRODUCT_PICTURE_PREFIX = "$PUBLIC_ROOT/$PRODUCT_ROOT/%s/pictures"
    }

    suspend fun putProductPicture(productId: ObjectId, file: FilePart): String {
        val key = "${PRODUCT_PICTURE_PREFIX.format(productId)}/${file.name()}"
        putFile(key, file)
        return key
    }

    private suspend fun putFile(key: String, file: FilePart): ObjectWriteResponse {
        val data = file.toByteArray()
        val request = PutObjectArgs.builder().apply {
            bucket(properties.bucket)
            `object`(key)
            contentType(file.headers().contentType?.toString())
            stream(ByteArrayInputStream(data), data.size.toLong(), -1L)
        }.build()
        return minioClient.putObject(request).await()
    }
}