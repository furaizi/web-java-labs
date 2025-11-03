package org.example.lab1_2.application.mapper

import org.example.lab1_2.application.dto.OrderDto
import org.example.lab1_2.application.dto.OrderLineDto
import org.example.lab1_2.application.dto.OrderStatusDto
import org.example.lab1_2.domain.order.Order
import org.example.lab1_2.domain.order.OrderLine

fun Order.toDto() = OrderDto(
    id = id.value,
    currency = currency.code(),
    status = when (status) {
        Order.Status.DRAFT     -> OrderStatusDto.DRAFT
        Order.Status.PAID      -> OrderStatusDto.PAID
        Order.Status.CANCELLED -> OrderStatusDto.CANCELLED
        },
    lines = lines.map { it.toDto() },
    total = total().amount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun OrderLine.toDto() = OrderLineDto(
    productId = productId.value,
    sku = sku,
    name = name,
    unitPrice = unitPrice.amount,
    quantity = quantity.value,
    lineTotal = lineTotal.amount
)