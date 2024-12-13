package com.flip.skateshop.web.rest

import com.flip.skateshop.interfaces.service.ProductServiceInterface
import com.flip.skateshop.web.rest.dto.CreateProductDto
import com.flip.skateshop.web.rest.dto.UpdateProductDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.bson.types.ObjectId
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductServiceInterface,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Add a new product")
    @ApiResponses(ApiResponse(responseCode = "200"))
    suspend fun addProduct(
        @RequestPart("productDto") productDto: CreateProductDto,
        @RequestPart("picture") picture: FilePart,
    ): String = productService.addProduct(productDto, picture).toHexString()

    @PutMapping("/{productId}")
    @Operation(summary = "Update a product")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "Product not found"),
    )
    suspend fun updateProduct(
        @PathVariable("productId") productId: ObjectId,
        @RequestBody productDto: UpdateProductDto,
    ) = productService.updateProduct(productId, productDto)

    @PutMapping("/{productId}/picture", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Update the picture of a product")
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "Product not found"),
    )
    suspend fun updateProductPicture(
        @PathVariable("productId") productId: ObjectId,
        @RequestPart("picture") picture: FilePart,
    ) = productService.updateProductPicture(productId, picture)

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete a product by ID")
    @ApiResponses(
        ApiResponse(responseCode = "204"),
        ApiResponse(responseCode = "404", description = "Product not found"),
    )
    suspend fun deleteProduct(
        @PathVariable("productId") productId: ObjectId,
    ) {
        productService.deleteProduct(productId)
    }
}
