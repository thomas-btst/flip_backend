package com.flip.skateshop.extention

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart

suspend fun FilePart.toByteArray(): ByteArray {
    return DataBufferUtils.join(content()).map { buffer ->
        ByteArray(buffer.readableByteCount()).also { bytes ->
            buffer.read(bytes)
            DataBufferUtils.release(buffer)
        }
    }.awaitSingle()
}
