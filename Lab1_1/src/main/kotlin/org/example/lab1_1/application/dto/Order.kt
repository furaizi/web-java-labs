package org.example.lab1_1.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class OrderStatusDto { DRAFT, PAID, CANCELLED }

data class OrderLineDto(
    val productId: UUID,
    val sku: String,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)

data class OrderDto(
    val id: UUID,
    val currency: String,
    val status: OrderStatusDto,
    val lines: List<OrderLineDto>,
    val total: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant
)