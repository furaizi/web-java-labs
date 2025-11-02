package org.example.lab1_1.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class ProductStatusDto { DRAFT, ACTIVE, ARCHIVED }

data class ProductDetailsDto(
    val id: UUID,
    val sku: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val currency: String,
    val categoryId: UUID? = null,
    val status: ProductStatusDto = ProductStatusDto.DRAFT,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ProductCreateDto(
    val sku: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val currency: String,
    val categoryId: UUID? = null,
    val status: ProductStatusDto = ProductStatusDto.DRAFT
)

data class ProductPatchDto(
    val sku: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val currency: String? = null,
    val categoryId: UUID? = null,
    val status: ProductStatusDto? = null
)