package com.flip.skateshop.web.rest

import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.service.ProductService
import com.flip.skateshop.web.rest.dto.ProductDto
import com.flip.skateshop.web.rest.dto.ProductPaginationDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public/products")
class PublicProductController(
    private val productService: ProductService,
) {
    @GetMapping("/limit/{limit}")
    @Operation(summary = "Retrieve a paginated list of products with optional filters")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun getProducts(
        @PathVariable limit: Int,
        @RequestParam pagination: ObjectId?,
        @RequestParam types: Set<ProductType> = emptySet(),
        @RequestParam minPrice: Long?,
        @RequestParam maxPrice: Long?,
        @RequestParam search: String = "",
    ): ProductPaginationDto =
        productService.getProducts(
            limit = limit,
            pagination = pagination,
            types = types,
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search,
        )

    @GetMapping("/{productId}")
    @Operation(summary = "Get a specific product by id")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "Product not found"),
    )
    suspend fun getProduct(
        @PathVariable productId: ObjectId,
    ): ProductDto = productService.getProduct(productId)
}
