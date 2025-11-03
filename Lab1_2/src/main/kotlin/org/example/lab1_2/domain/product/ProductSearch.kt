package org.example.lab1_2.domain.product

import java.math.BigDecimal
import java.util.UUID

data class ProductSearch(
    val q: String? = null,
    val categoryId: UUID? = null,
    val status: Product.Status? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null
)

enum class ProductSortField {
    NAME, PRICE, CREATED_AT, UPDATED_AT, SKU
}