package org.example.lab1_1.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CartItemDto(
    val productId: UUID,
    val sku: String,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int
)

data class CartDto(
    val id: UUID,
    val currency: String,
    val items: List<CartItemDto>,
    val subtotal: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant
)