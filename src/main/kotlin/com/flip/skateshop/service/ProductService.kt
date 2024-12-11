package com.flip.skateshop.service

import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.interfaces.repository.ProductRepositoryInterface
import com.flip.skateshop.interfaces.service.FileServiceInterface
import com.flip.skateshop.interfaces.service.ProductServiceInterface
import com.flip.skateshop.mapper.ProductMapper
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.ProductDto
import com.flip.skateshop.web.rest.dto.ProductPaginationDto
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ProductService(
    private val productRepository: ProductRepositoryInterface,
    private val productMapper: ProductMapper,
    private val fileService: FileServiceInterface,
) : ProductServiceInterface {
    override suspend fun getProduct(productId: ObjectId): ProductDto {
        val product =
            productRepository.findById(productId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product with id $productId does not exist")
        return productMapper.toProductDto(product)
    }

    override suspend fun getProducts(
        limit: Int,
        pagination: ObjectId?,
        types: Set<ProductType>,
        minPrice: Long?,
        maxPrice: Long?,
        search: String,
    ): ProductPaginationDto {
        val (products, hasMore) =
            productRepository.findByFilterPaginated(
                limit = limit,
                pagination = pagination,
                types = types,
                minPrice = minPrice,
                maxPrice = maxPrice,
                search = search.trim(),
            )
        return ProductPaginationDto(products.map(productMapper::toProductDto), hasMore)
    }

    override suspend fun addProduct(
        productDto: CreateProductDto,
        picture: FilePart,
    ): ObjectId {
        val productId = ObjectId()
        val path: String = fileService.putProductPicture(productId, picture)
        return productRepository.save(productMapper.toProduct(productId, productDto, path))._id
    }
}
