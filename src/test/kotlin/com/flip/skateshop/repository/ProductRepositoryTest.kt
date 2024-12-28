package com.flip.skateshop.repository

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.UpdateProductDto
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductRepositoryTest(
    @Autowired
    private val productRepository: ProductRepositoryInterface,
) : ServicesCleaner() {
    private fun product(
        name: String = "Nom",
        description: String = "Description",
        price: Long = 800,
        picture: String = "/path/to/file",
    ): Product =
        Product.Skate(
            ObjectId(),
            name,
            description,
            price,
            picture,
        )

    @Test
    fun `should save a product successfully and find it by id`() =
        runTest {
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
    fun `should delete a product correctly`() =
        runTest {
            val product = product()
            productRepository.save(product)
            productRepository.deleteById(product._id)
            assertNull(productRepository.findById(product._id))
        }

    @Test
    fun `should update a product correctly`() =
        runTest {
            val product = product("Name", "Description", 800)
            productRepository.save(product)
            val newProduct = UpdateProductDto.Wheel("New name", "New description", 400)
            productRepository.updateProduct(product._id, newProduct)
            val updatedProduct = productRepository.findById(product._id)
            assertNotNull(updatedProduct)
            newProduct.run {
                assertEquals(name, updatedProduct.name)
                assertEquals(description, updatedProduct.description)
                assertEquals(price, updatedProduct.price)
                assert(updatedProduct is Product.Wheel)
            }
        }

    @Test
    fun `should not update a product if there is not changes`() =
        runTest {
            val product = product()
            productRepository.save(product)
            productRepository.updateProduct(product._id, UpdateProductDto.Skate(null, null, null))
            val updatedProduct = productRepository.findById(product._id)
            assertNotNull(updatedProduct)
            product.run {
                assertEquals(name, updatedProduct.name)
                assertEquals(description, updatedProduct.description)
                assertEquals(price, updatedProduct.price)
                assert(updatedProduct is Product.Skate)
            }
        }

    @Test
    fun `should update picture successfully`() =
        runTest {
            val product = product(picture = "/path/to/file")
            productRepository.save(product)
            val newPath = "/new/path/to/file"
            productRepository.updatePicture(product._id, newPath)
            val updatedProduct = productRepository.findById(product._id)
            assertNotNull(updatedProduct)
            assertEquals(newPath, updatedProduct.picture)
        }

    @Test
    fun `should count products correctly`() =
        runTest {
            val count = 10L
            for (i in 1..count) {
                productRepository.save(product())
            }
            assertEquals(count, productRepository.count())
        }

    @Test
    fun `should filter products correctly`() =
        runTest {
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
    fun `should paginate products successfully`() =
        runTest {
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
            assertEquals(false, secondPagination.second)
        }

    @Test
    fun `should list products in page correctly`() =
        runTest {
            val limit = 20
            val productsNumber = 30L
            for (i in 1..productsNumber) {
                productRepository.save(product())
            }

            val firstPagination =
                productRepository.findByNameLikeAndByTypeAndByPage(
                    limit,
                    0,
                    "",
                    null,
                )
            val secondPagination =
                productRepository.findByNameLikeAndByTypeAndByPage(
                    limit,
                    1,
                    "",
                    null,
                )

            assertEquals(limit, firstPagination.first.toList().size)
            assertEquals(productsNumber.toInt() - limit, secondPagination.first.toList().size)
            assertEquals(productsNumber, firstPagination.second)
            assertEquals(productsNumber, secondPagination.second)
        }

    @Test
    fun `should search products by name and type in page`() =
        runTest {
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
                productRepository.findByNameLikeAndByTypeAndByPage(
                    20,
                    0,
                    "",
                    null,
                )
            val findWheel =
                productRepository.findByNameLikeAndByTypeAndByPage(
                    20,
                    0,
                    "",
                    ProductType.WHEEL,
                )
            val findNameSkate =
                productRepository.findByNameLikeAndByTypeAndByPage(
                    20,
                    0,
                    "kate ",
                    null,
                )
            val findBadName =
                productRepository.findByNameLikeAndByTypeAndByPage(
                    20,
                    0,
                    "skateboard",
                    null,
                )

            assertEquals(3, findAll.first.toList().size)
            assertEquals(3, findAll.second)
            assertEquals(1, findWheel.first.toList().size)
            assertEquals(1, findWheel.second)
            assertEquals(2, findNameSkate.first.toList().size)
            assertEquals(2, findNameSkate.second)
            assertEquals(0, findBadName.first.toList().size)
            assertEquals(0, findBadName.second)
        }
}
