package com.flip.skateshop.web.rest

import com.flip.skateshop.domain.ProductType
import com.flip.skateshop.service.ProductService
import com.flip.skateshop.web.rest.dto.ProductPaginationDto
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/public/products")
class PublicProductController(
    private val productService: ProductService,
) {
    @GetMapping("/limit/{limit}")
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
}
