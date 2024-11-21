package com.flip.skateshop.web.rest

import com.flip.skateshop.service.ProductService
import com.flip.skateshop.web.rest.dto.CreateProductDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Add a new product")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun addProduct(
        @RequestPart("productDto") productDto: CreateProductDto,
        @RequestPart("picture") picture: FilePart,
    ): String = productService.addProduct(productDto, picture).toHexString()
}
