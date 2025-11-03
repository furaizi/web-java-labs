package org.example.lab1_1.application.service

import org.example.lab1_1.application.dto.*
import org.example.lab1_1.domain.common.PageResult
import org.example.lab1_1.domain.product.Product
import java.util.*

interface ProductService {
    fun create(dto: ProductCreateDto): ProductDetailsDto
    fun get(id: UUID): ProductDetailsDto
    fun list(
        q: String? = null,
        categoryId: UUID? = null,
        status: Product.Status? = null,
        minPrice: java.math.BigDecimal? = null,
        maxPrice: java.math.BigDecimal? = null,
        page: Int = 0,
        size: Int = 20,
        sort: List<String> = emptyList()
    ): PageResult<ProductDetailsDto>

    fun replace(id: UUID, dto: ProductCreateDto): ProductDetailsDto
    fun patch(id: UUID, dto: ProductPatchDto): ProductDetailsDto
    fun delete(id: UUID)
}