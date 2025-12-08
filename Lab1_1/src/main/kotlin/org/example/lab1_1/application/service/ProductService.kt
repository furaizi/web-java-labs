package org.example.lab1_1.application.service

import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductDetailsDto
import org.example.lab1_1.application.dto.ProductPatchDto
import org.example.lab1_1.application.dto.ProductStatusDto
import org.example.lab1_1.domain.common.PageResult
import org.example.lab1_1.domain.repository.projection.ProductSalesProjection
import java.math.BigDecimal
import java.util.UUID

interface ProductService {
    fun list(
        q: String? = null,
        categoryId: UUID? = null,
        status: ProductStatusDto? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        page: Int = 0,
        size: Int = 20,
        sort: List<String> = emptyList()
    ): PageResult<ProductDetailsDto>

    fun create(dto: ProductCreateDto): ProductDetailsDto
    fun get(id: UUID): ProductDetailsDto
    fun replace(id: UUID, dto: ProductCreateDto): ProductDetailsDto
    fun patch(id: UUID, dto: ProductPatchDto): ProductDetailsDto
    fun delete(id: UUID)
    fun findTopSelling(limit: Int = 10): List<ProductSalesProjection>
}
