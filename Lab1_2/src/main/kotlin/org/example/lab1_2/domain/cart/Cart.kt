package org.example.lab1_2.domain.cart

import org.example.lab1_2.domain.common.AggregateRoot
import org.example.lab1_2.domain.common.Money
import org.example.lab1_2.domain.common.Quantity
import org.example.lab1_2.domain.order.Order
import org.example.lab1_2.domain.order.OrderId
import org.example.lab1_2.domain.order.OrderLine
import org.example.lab1_2.domain.product.ProductId
import java.time.Instant
import java.util.*

@JvmInline
value class CartId(val value: UUID)

data class CartItem(
    val productId: ProductId,
    val sku: String,
    val name: String,
    val unitPrice: Money,
    val quantity: Quantity
) {
    init {
        require(name.isNotBlank())
    }
}

class Cart(
    val id: CartId,
    val currency: Currency,
    items: List<CartItem> = emptyList(),
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = createdAt
) : AggregateRoot {

    private val _items = items.toMutableList()
    val items: List<CartItem>
        get() = _items.toList()

    fun addItem(item: CartItem) {
        require(item.unitPrice.currency == currency) {
            "currency mismatch"
        }

        val existing = _items.indexOfFirst {
            it.productId == item.productId
        }

        if (existing >= 0) {
            val old = _items[existing]
            _items[existing] = old.copy(
                quantity = Quantity(old.quantity.value + item.quantity.value)
            )
        } else {
            _items += item
        }

        touch()
    }

    fun removeItem(productId: ProductId) {
        _items.removeIf {
            it.productId == productId
        }
        touch()
    }

    fun clear() {
        _items.clear()
        touch()
    }

    fun subtotal(): Money =
        _items.fold(Money.zero(currency)) {
            acc, i -> acc + (i.unitPrice * i.quantity)
        }.normalized()

    fun checkout(
        orderId: OrderId = OrderId(UUID.randomUUID())
    ): Order {
        require(_items.isNotEmpty()) {
            "cart is empty"
        }

        val lines = _items.map { i ->
            OrderLine(
                productId = i.productId,
                sku = i.sku,
                name = i.name,
                unitPrice = i.unitPrice,
                quantity = i.quantity
            )
        }

        return Order(
            id = orderId,
            currency = currency,
            lines = lines,
            status = Order.Status.DRAFT
        )
    }

    private fun touch() {
        updatedAt = Instant.now()
    }
}
