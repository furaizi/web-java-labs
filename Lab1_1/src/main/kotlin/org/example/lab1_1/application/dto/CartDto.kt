package org.example.lab1_1.application.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.example.lab1_1.application.validation.annotation.CosmicName
import org.example.lab1_1.application.validation.annotation.CurrencyCode
import org.example.lab1_1.application.validation.annotation.MoneyAmount
import org.example.lab1_1.application.validation.annotation.SkuCode
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CartItemDto(
    val productId: UUID,

    @field:SkuCode
    val sku: String,

    @field:CosmicName
    val name: String,

    @field:MoneyAmount
    val unitPrice: BigDecimal,

    @field:Min(1)
    val quantity: Int
)

data class CartDto(
    val id: UUID,

    @field:CurrencyCode
    val currency: String,

    @field:NotEmpty
    @field:Valid
    val items: List<CartItemDto>,

    val subtotal: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant
)