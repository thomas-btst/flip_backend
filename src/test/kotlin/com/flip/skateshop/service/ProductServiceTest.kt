package com.flip.skateshop.service

import com.flip.skateshop.domain.Product
import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.interfaces.repository.FeedbackRepositoryInterface
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.mapper.ProductMapper
import com.flip.skateshop.util.ServicesCleaner
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.UpdateProductDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductServiceTest
    @Autowired
    constructor(
        private val productRepository: ProductRepositoryInterface,
        private val productMapper: ProductMapper,
        private val feedbackRepository: FeedbackRepositoryInterface,
    ) : ServicesCleaner() {
        private val fileService = mockk<FileServiceInterface>(relaxed = true)
        private val productService = ProductService(productRepository, productMapper, fileService, feedbackRepository)

        suspend fun createProduct(
            name: String = "Name",
            description: String = "Description",
            price: Long = 8,
            picture: String = "/path/to/file",
        ): Product {
            val product =
                Product.Skate(
                    ObjectId(),
                    name,
                    description,
                    price,
                    picture,
                )
            productRepository.save(product)
            return product
        }

        suspend fun createProducts(number: Int) {
            for (i in 1..number) {
                createProduct()
            }
        }

        @Test
        fun `should add a product successfully`() =
            runTest {
                val productDto = CreateProductDto.Skate("Name", "Description", 8)
                val file = mockk<FilePart>(relaxed = true)
                val productId =
                    productService.addProduct(
                        productDto,
                        file,
                    )
                coVerify { fileService.putProductPicture(any(), file) }
                val product = productRepository.findById(productId)
                assertNotNull(product)
                assertNotNull(product.picture)
                productMapper
                    .toProduct(
                        productId,
                        productDto,
                        product.picture,
                    ).run {
                        assertEquals(name, product.name)
                        assertEquals(description, product.description)
                        assertEquals(price, product.price)
                        assertEquals(
                            productMapper.toProductDto(this).type,
                            productMapper.toProductDto(product).type,
                        )
                    }
            }

        @Test
        fun `should list products successfully`() =
            runTest {
                assertEquals(
                    0,
                    productService
                        .getProducts(
                            20,
                            null,
                            emptySet(),
                            0,
                            0,
                            "",
                        ).products
                        .toList()
                        .size,
                )

                val productsNumber = 4
                createProducts(productsNumber)
                assertEquals(
                    productsNumber,
                    productService
                        .getProducts(
                            20,
                            null,
                            emptySet(),
                            null,
                            null,
                            "",
                        ).products
                        .toList()
                        .size,
                )
            }

        @Test
        fun `should delete a product correctly`() =
            runTest {
                val product = createProduct()
                productService.deleteProduct(product._id)
                coVerify { fileService.deleteFile(product.picture) }
                assertNull(productRepository.findById(product._id))
            }

        @Test
        fun `should not delete a product if it does not exists`() =
            runTest {
                val exception = assertThrows<ResponseStatusException> { productService.deleteProduct(ObjectId()) }
                assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
            }

        @Test
        fun `should update a product successfully`() =
            runTest {
                val product = createProduct("Name", "Description", 8)
                productRepository.save(product)
                val newProduct = UpdateProductDto.Deck("New name", "New description", 3)
                productService.updateProduct(product._id, newProduct)
                val updatedProduct = productRepository.findById(product._id)
                assertNotNull(updatedProduct)
                newProduct.run {
                    assertEquals(name, updatedProduct.name)
                    assertEquals(description, updatedProduct.description)
                    assertEquals(price, updatedProduct.price)
                    assert(updatedProduct is Product.Deck)
                }
            }

        @Test
        fun `should update product throw error if product does not exists`() =
            runTest {
                assertThrows<ResponseStatusException> {
                    productService.updateProduct(ObjectId(), UpdateProductDto.Deck(null, null, null))
                }.let { assertEquals(HttpStatus.NOT_FOUND, it.statusCode) }
            }

        @Test
        fun `should update product picture successfully`() =
            runTest {
                val product = createProduct(picture = "/path/to/file")
                productRepository.save(product)
                val file = mockk<FilePart>()
                val key = "/new/path/to/file"
                coEvery { fileService.putProductPicture(product._id, file) } returns key
                productService.updateProductPicture(product._id, file)
                coVerify { fileService.deleteFile(product.picture) }
                coVerify { fileService.putProductPicture(product._id, file) }
                val updatedProduct = productRepository.findById(product._id)
                assertNotNull(updatedProduct)
                assertEquals(key, updatedProduct.picture)
            }

        @Test
        fun `should update product picture throw error if product does not exists`() =
            runTest {
                assertThrows<ResponseStatusException> {
                    productService.updateProductPicture(ObjectId(), mockk<FilePart>())
                }.let { assertEquals(HttpStatus.NOT_FOUND, it.statusCode) }
            }

        @Test
        fun `should paginate products correctly`() =
            runTest {
                val limit = 20
                val productsNumber = 30
                createProducts(productsNumber)

                val firstPagination =
                    productService.getProducts(
                        limit,
                        null,
                        emptySet(),
                        null,
                        null,
                        "",
                    )
                val secondPagination =
                    productService.getProducts(
                        limit,
                        ObjectId(firstPagination.products.last().id),
                        emptySet(),
                        null,
                        null,
                        "",
                    )

                assertEquals(limit, firstPagination.products.size)
                assertEquals(productsNumber - limit, secondPagination.products.size)
                assert(firstPagination.hasMore)
                assertEquals(secondPagination.hasMore, false)
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
                    productService.getProducts(
                        20,
                        null,
                        emptySet(),
                        null,
                        null,
                        "",
                    )
                val findWheel =
                    productService.getProducts(
                        20,
                        null,
                        setOf(ProductType.WHEEL),
                        null,
                        null,
                        "",
                    )
                val findNameSkate =
                    productService.getProducts(
                        20,
                        null,
                        emptySet(),
                        null,
                        null,
                        "kate ",
                    )
                val findBadName =
                    productService.getProducts(
                        20,
                        null,
                        emptySet(),
                        null,
                        null,
                        "skateboard",
                    )
                val findByPrice =
                    productService.getProducts(
                        20,
                        null,
                        emptySet(),
                        9,
                        11,
                        "",
                    )

                assertEquals(3, findAll.products.size)
                assertEquals(1, findWheel.products.size)
                assertEquals(2, findNameSkate.products.size)
                assertEquals(0, findBadName.products.size)
                assertEquals(1, findByPrice.products.size)
            }

        @Test
        fun `should retrieve a product correctly`() =
            runTest {
                val product = createProduct()
                val productDto = productService.getProduct(product._id)
                product.run {
                    assertEquals(name, productDto.name)
                    assertEquals(description, productDto.description)
                    assertEquals(price, productDto.price)
                    assertEquals(productMapper.toProductType(product), productDto.type)
                    assertEquals(null, productDto.rate)
                }
            }
    }
