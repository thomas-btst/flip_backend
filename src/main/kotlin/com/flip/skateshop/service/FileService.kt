package com.flip.skateshop.service

import com.flip.skateshop.config.MinioConfig.Companion.PRIVATE_ROOT
import com.flip.skateshop.config.MinioConfig.Companion.PUBLIC_ROOT
import com.flip.skateshop.config.SkateshopProperties
import com.flip.skateshop.extention.toByteArray
import com.flip.skateshop.interfaces.service.FileServiceInterface
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioAsyncClient
import io.minio.ObjectWriteResponse
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.http.Method
import kotlinx.coroutines.future.await
import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.util.concurrent.*

@Service
class FileService(
    private val minioClient: MinioAsyncClient,
    skateshopProperties: SkateshopProperties,
) : FileServiceInterface {
    private val properties = skateshopProperties.minio

    companion object {
        const val PRODUCT_ROOT = "products"
        const val USER_ROOT = "users"
        const val INVOICE_ROOT = "$USER_ROOT/%s/invoices"
        const val PRODUCT_PICTURE_PREFIX = "$PUBLIC_ROOT/$PRODUCT_ROOT/%s/pictures"
        const val USER_LOGO_PREFIX = "$PRIVATE_ROOT/$USER_ROOT/%s/logos"
        const val COMMAND_INVOICE_PREFIX = "$PRIVATE_ROOT/$INVOICE_ROOT/%s"
    }

    override suspend fun putUserLogo(
        userId: ObjectId,
        file: FilePart,
    ): String = putUserLogo(userId, file.name(), file.toByteArray(), file.headers().contentType.toString())

    override suspend fun putUserLogo(
        userId: ObjectId,
        filename: String,
        data: ByteArray,
        type: String?,
    ): String {
        val key = "${USER_LOGO_PREFIX.format(userId)}/$filename"
        putFile(key, data, type)
        return key
    }

    override suspend fun putProductPicture(
        productId: ObjectId,
        file: FilePart,
    ): String = putProductPicture(productId, file.name(), file.toByteArray(), file.headers().contentType.toString())

    override suspend fun putProductPicture(
        productId: ObjectId,
        filename: String,
        data: ByteArray,
        type: String?,
    ): String {
        val key = "${PRODUCT_PICTURE_PREFIX.format(productId)}/$filename"
        putFile(key, data, type)
        return key
    }

    override suspend fun putCommandInvoice(
        userId: ObjectId,
        invoiceId: ObjectId,
        filename: String,
        data: ByteArray,
        type: String?,
    ): String {
        val key = "${COMMAND_INVOICE_PREFIX.format(userId, invoiceId)}/$filename"
        putFile(key, data, type)
        return key
    }

    override suspend fun deleteFile(key: String) {
        val request =
            RemoveObjectArgs
                .builder()
                .apply {
                    bucket(properties.bucket)
                    `object`(key)
                }.build()
        minioClient.removeObject(request).await()
    }

    private suspend fun putFile(
        key: String,
        data: ByteArray,
        contentType: String?,
    ): ObjectWriteResponse {
        val request =
            PutObjectArgs
                .builder()
                .apply {
                    bucket(properties.bucket)
                    `object`(key)
                    contentType(contentType?.toString())
                    stream(ByteArrayInputStream(data), data.size.toLong(), -1L)
                }.build()
        return minioClient.putObject(request).await()
    }

    override suspend fun signUrl(fileKey: String): String {
        val request =
            GetPresignedObjectUrlArgs
                .builder()
                .method(Method.GET)
                .bucket(properties.bucket)
                .`object`(fileKey)
                .expiry(1, TimeUnit.HOURS)
                .build()
        return minioClient.getPresignedObjectUrl(request)
    }
}
