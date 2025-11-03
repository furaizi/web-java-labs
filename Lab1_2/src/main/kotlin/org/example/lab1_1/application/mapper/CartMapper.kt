package org.example.lab1_1.application.mapper

import org.example.lab1_1.application.dto.CartDto
import org.example.lab1_1.application.dto.CartItemDto
import org.example.lab1_1.domain.cart.Cart
import org.example.lab1_1.domain.cart.CartId
import org.example.lab1_1.domain.cart.CartItem
import org.example.lab1_1.domain.common.Quantity
import org.example.lab1_1.domain.product.ProductId
import java.util.UUID

fun Cart.toDto() = CartDto(
    id = id.value,
    currency = currency.code(),
    items = items.map { it.toDto() },
    subtotal = subtotal().amount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun CartItem.toDto() = CartItemDto(
    productId = productId.value,
    sku = sku,
    name = name,
    unitPrice = unitPrice.amount,
    quantity = quantity.value
)

object CartFactory {
    fun empty(
        currency: String,
        id: UUID = UUID.randomUUID()
    ) = Cart(
        id = CartId(id),
        currency = currency.toCurrency()
    )

    fun fromDto(dto: CartDto) = Cart(
        id = CartId(dto.id),
        currency = dto.currency.toCurrency(),
        items = dto.items.map {
            CartItem(
                productId = ProductId(it.productId),
                sku = it.sku,
                name = it.name,
                unitPrice = it.unitPrice.asMoney(dto.currency.toCurrency()),
                quantity = Quantity(it.quantity)
            )
        }
    )
}