package com.flip.skateshop.service

import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.mapper.FileMapper
import com.flip.skateshop.util.ServicesCleaner
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import java.net.URL
import javax.imageio.IIOException
import javax.imageio.ImageIO

class FileServiceTest(
    @Autowired private val fileService: FileServiceInterface,
    @Autowired private val fileMapper: FileMapper,
) : ServicesCleaner() {
    @Test
    fun `should put a product picture correctly`() =
        runTest {
            val productId = ObjectId()
            val picture = ClassPathResource("picture/logo.jpeg").file.readBytes()
            val key = fileService.putProductPicture(productId, "picture", picture, MediaType.IMAGE_JPEG.toString())
            ImageIO.read(URL(fileMapper.toPublicPath(key)))
        }

    @Test
    fun `should put an user logo correctly`() =
        runTest {
            val userId = ObjectId()
            val picture = ClassPathResource("picture/logo.jpeg").file.readBytes()
            val key = fileService.putUserLogo(userId, "picture", picture, MediaType.IMAGE_JPEG.toString())
            ImageIO.read(URL(fileMapper.toPublicPath(key)))
        }

    @Test
    fun `should delete a file successfully`() =
        runTest {
            val userId = ObjectId()
            val picture = ClassPathResource("picture/logo.jpeg").file.readBytes()
            val key = fileService.putProductPicture(userId, "picture", picture, MediaType.IMAGE_JPEG.toString())
            fileService.deleteFile(key)
            assertThrows<IIOException> {
                ImageIO.read(URL(fileMapper.toPublicPath(key)))
            }
        }

    @Test
    fun `should put an invoice correctly`() =
        runTest {
            val invoice = "html"
            val key =
                fileService.putCommandInvoice(
                    ObjectId(),
                    ObjectId(),
                    "invoice.html",
                    invoice.toByteArray(),
                    MediaType.TEXT_HTML.toString(),
                )
            ImageIO.read(URL(fileMapper.toPublicPath(key)))
        }
}
