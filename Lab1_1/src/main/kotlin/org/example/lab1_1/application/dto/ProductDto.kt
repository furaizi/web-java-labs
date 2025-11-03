package org.example.lab1_1.application.dto

import jakarta.validation.constraints.Size
import org.example.lab1_1.application.validation.annotation.CosmicName
import org.example.lab1_1.application.validation.annotation.CurrencyCode
import org.example.lab1_1.application.validation.annotation.MoneyAmount
import org.example.lab1_1.application.validation.annotation.SkuCode
import org.example.lab1_1.domain.product.Product
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID


data class ProductDetailsDto(
    val id: UUID,
    val sku: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val currency: String,
    val categoryId: UUID? = null,
    val status: Product.Status = Product.Status.DRAFT,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ProductCreateDto(
    @field:SkuCode
    val sku: String,

    @field:CosmicName
    val name: String,

    @field:Size(max = 2000)
    val description: String? = null,

    @field:MoneyAmount
    val price: BigDecimal,

    @field:CurrencyCode
    val currency: String,

    val categoryId: UUID? = null,

    val status: Product.Status = Product.Status.DRAFT,
)

data class ProductPatchDto(
    @field:SkuCode
    val sku: String? = null,

    @field:CosmicName
    val name: String? = null,

    @field:Size(max = 2000)
    val description: String? = null,

    @field:MoneyAmount
    val price: BigDecimal? = null,

    @field:CurrencyCode
    val currency: String? = null,

    val categoryId: UUID? = null,

    val status: Product.Status? = null
)