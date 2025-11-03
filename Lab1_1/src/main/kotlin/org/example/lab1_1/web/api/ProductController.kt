package org.example.lab1_1.web.api

import jakarta.validation.Valid
import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductDetailsDto
import org.example.lab1_1.application.dto.ProductPatchDto
import org.example.lab1_1.application.service.ProductService
import org.example.lab1_1.domain.common.PageResult
import org.example.lab1_1.domain.product.Product
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val service: ProductService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(name = "category", required = false) categoryId: UUID?,
        @RequestParam(required = false) status: Product.Status?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: List<String> = emptyList()
    ): PageResult<ProductDetailsDto> =
        service.list(
            q = q,
            categoryId = categoryId,
            status = status,
            minPrice = minPrice,
            maxPrice = maxPrice,
            page = page,
            size = size,
            sort = sort
        )

    @PostMapping
    fun create(@Valid @RequestBody dto: ProductCreateDto): ResponseEntity<ProductDetailsDto> {
        val created = service.create(dto)
        val location = URI.create("/api/v1/products/${created.id}")
        return ResponseEntity.created(location)
            .body(created)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID) = service.get(id)

    @PutMapping("/{id}")
    fun replace(
        @PathVariable id: UUID,
        @Valid @RequestBody dto: ProductCreateDto
    ) = service.replace(id, dto)

    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: UUID,
        @Valid @RequestBody dto: ProductPatchDto
    ) = service.patch(id, dto)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        service.delete(id)
    }

}