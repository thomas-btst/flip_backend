package com.flip.skateshop.repository

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.util.ServicesCleaner
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProductRepositoryTest(
    @Autowired
    private val productRepository: ProductRepositoryInterface
): ServicesCleaner() {
    private fun product(
        name: String = "Nom",
        description: String = "Description",
        price: Long = 800,
        picture: String = "/path/to/file"
    ): Product {
        return Product.Skate(
            ObjectId(),
            name,
            description,
            price,
            picture,
        )
    }

    @Test
    fun `should save a product successfully and find it by id`() = runTest {
        val product = product()
        productRepository.save(product)
        val searchedProduct = productRepository.findById(product._id)
        assertNotNull(searchedProduct)
        product.run {
            assertEquals(name, searchedProduct.name)
            assertEquals(description, searchedProduct.description)
            assertEquals(price, searchedProduct.price)
            assertEquals(picture, searchedProduct.picture)
        }
    }

    @Test
    fun `should count products correctly`() = runTest {
        val count = 10L
        for (i in 1..count) {
            productRepository.save(product())
        }
        assertEquals(count, productRepository.count())
    }

    @Test
    fun `should filter products correctly`() = runTest {
        productRepository.save(
            Product.Skate(
                ObjectId(),
                "Skate 1",
                "Description",
                8,
                "",
            ),
        )
        productRepository.save(
            Product.Skate(
                ObjectId(),
                "Skate 2",
                "Description",
                10,
                "",
            ),
        )
        productRepository.save(
            Product.Wheel(
                ObjectId(),
                "Wheel 1",
                "Description",
                12,
                "",
            ),
        )

        val findAll =
            productRepository.findByFilterPaginated(
                20,
                null,
                emptySet(),
                null,
                null,
                "",
            )
        val findWheel =
            productRepository.findByFilterPaginated(
                20,
                null,
                setOf(ProductType.WHEEL),
                null,
                null,
                "",
            )
        val findNameSkate =
            productRepository.findByFilterPaginated(
                20,
                null,
                emptySet(),
                null,
                null,
                "kate ",
            )
        val findBadName =
            productRepository.findByFilterPaginated(
                20,
                null,
                emptySet(),
                null,
                null,
                "skateboard",
            )
        val findByPrice =
            productRepository.findByFilterPaginated(
                20,
                null,
                emptySet(),
                9,
                11,
                "",
            )

        assertEquals(3, findAll.first.size)
        assert(!findAll.second)
        assertEquals(1, findWheel.first.size)
        assert(!findWheel.second)
        assertEquals(2, findNameSkate.first.size)
        assert(!findNameSkate.second)
        assertEquals(0, findBadName.first.size)
        assert(!findBadName.second)
        assertEquals(1, findByPrice.first.size)
        assert(!findByPrice.second)
    }

    @Test
    fun `should paginate products successfully`() = runTest {
        val limit = 20
        val productsNumber = 30
        for (i in 1..productsNumber) {
            productRepository.save(product())
        }

        val firstPagination =
            productRepository.findByFilterPaginated(
                limit,
                null,
                emptySet(),
                null,
                null,
                "",
            )
        val secondPagination =
            productRepository.findByFilterPaginated(
                limit,
                firstPagination.first.last()._id,
                emptySet(),
                null,
                null,
                "",
            )

        assertEquals(limit, firstPagination.first.size)
        assertEquals(productsNumber - limit, secondPagination.first.size)
        assert(firstPagination.second)
        assertEquals(secondPagination.second, false)
    }
}