package org.example.lab1_1.application.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import org.example.lab1_1.application.validation.annotation.CosmicName
import org.example.lab1_1.application.validation.annotation.CurrencyCode
import org.example.lab1_1.application.validation.annotation.MoneyAmount
import org.example.lab1_1.application.validation.annotation.SkuCode
import java.math.BigDecimal
import java.time.Instant
import java.util.*

enum class OrderStatusDto { DRAFT, PAID, CANCELLED }

data class OrderLineDto(
    val productId: UUID,

    @field:SkuCode
    val sku: String,

    @field:CosmicName
    val name: String,

    @field:MoneyAmount
    val unitPrice: BigDecimal,

    @field:Min(1)
    val quantity: Int,

    @field:MoneyAmount
    val lineTotal: BigDecimal
)

data class OrderDto(
    val id: UUID,

    @field:CurrencyCode
    val currency: String,

    val status: OrderStatusDto,

    @field:NotEmpty
    @field:Valid
    val lines: List<OrderLineDto>,

    @field:MoneyAmount
    val total: BigDecimal,

    val createdAt: Instant,
    val updatedAt: Instant
)