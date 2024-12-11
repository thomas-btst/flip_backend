package com.flip.skateshop.seeder

import com.flip.skateshop.config.Seeder
import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files

@Component
class ProductSeeder(
    private val productRepository: ProductRepositoryInterface,
    private val fileService: FileServiceInterface,
) : Seeder("products") {
    fun randomPicture(type: ProductType): File? {
        val pictures = ClassPathResource("seed/product/${type.name.lowercase()}").file.listFiles() ?: return null
        if (pictures.isEmpty()) {
            return null
        }
        return pictures.random()
    }

    fun seedProduct() {
        val productId = ObjectId()
        val name = faker.commerce().productName()
        val description = faker.lorem().sentence(10)
        val price = (faker.number().randomDouble(2, 5, 180) * 100).toLong()
        val type = ProductType.entries.random()
        val picture = randomPicture(type) ?: return

        runBlocking {
            val pictureKey =
                fileService.putProductPicture(
                    productId,
                    picture.name,
                    picture.readBytes(),
                    Files.probeContentType(picture.toPath()) ?: MediaType.IMAGE_PNG.toString(),
                )

            when (type) {
                ProductType.SKATE -> Product.Skate(productId, name, description, price, pictureKey)
                ProductType.DECK -> Product.Deck(productId, name, description, price, pictureKey)
                ProductType.WHEEL -> Product.Wheel(productId, name, description, price, pictureKey)
                ProductType.BEARING -> Product.Bearing(productId, name, description, price, pictureKey)
                ProductType.GRID_TAPE -> Product.GridTape(productId, name, description, price, pictureKey)
                ProductType.TRUCK -> Product.Truck(productId, name, description, price, pictureKey)
            }.let { product -> productRepository.save(product) }
        }
    }

    override fun seed() {
        for (i in 1..200) {
            seedProduct()
        }
    }
}
