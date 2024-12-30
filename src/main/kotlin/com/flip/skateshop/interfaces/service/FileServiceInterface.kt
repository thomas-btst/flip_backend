package com.flip.skateshop.interfaces.service

import org.bson.types.ObjectId
import org.springframework.http.codec.multipart.FilePart

interface FileServiceInterface {
    suspend fun putUserLogo(
        userId: ObjectId,
        file: FilePart,
    ): String

    suspend fun putUserLogo(
        userId: ObjectId,
        filename: String,
        data: ByteArray,
        type: String?,
    ): String

    suspend fun putProductPicture(
        productId: ObjectId,
        file: FilePart,
    ): String

    suspend fun putProductPicture(
        productId: ObjectId,
        filename: String,
        data: ByteArray,
        type: String?,
    ): String

    suspend fun putCommandInvoice(
        userId: ObjectId,
        invoiceId: ObjectId,
        filename: String,
        data: ByteArray,
        type: String?,
    ): String

    suspend fun deleteFile(key: String)

    suspend fun signUrl(fileKey: String): String
}
