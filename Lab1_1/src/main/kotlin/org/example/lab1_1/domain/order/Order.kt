package org.example.lab1_1.domain.order

import org.example.lab1_1.domain.common.AggregateRoot
import org.example.lab1_1.domain.common.Money
import org.example.lab1_1.domain.common.Quantity
import org.example.lab1_1.domain.product.ProductId
import java.time.Instant
import java.util.*

@JvmInline
value class OrderId(val value: UUID)

data class OrderLine(
    val productId: ProductId,
    val sku: String,
    val name: String,
    val unitPrice: Money,
    val quantity: Quantity
) {
    init {
        require(name.isNotBlank())
    }

    val lineTotal: Money
        get() = unitPrice * quantity
}

class Order(
    val id: OrderId,
    val currency: Currency,
    lines: List<OrderLine> = emptyList(),
    status: Status = Status.DRAFT,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = createdAt
) : AggregateRoot {

    enum class Status {
        DRAFT, PAID, CANCELLED
    }

    private val _lines = lines.toMutableList()
    val lines: List<OrderLine>
        get() = _lines.toList()

    var status: Status = status
        private set

    fun addLine(line: OrderLine) {
        require(line.unitPrice.currency == currency) {
            "currency mismatch"
        }

        _lines += line
        touch()
    }

    fun removeLine(productId: ProductId) {
        _lines.removeIf {
            it.productId == productId
        }

        touch()
    }

    fun total(): Money =
        _lines.fold(Money.zero(currency)) {
            acc, l -> acc + l.lineTotal
        }.normalized()

    fun pay() {
        require(status == Status.DRAFT) {
            "only draft can be paid"
        }

        status = Status.PAID
        touch()
    }

    fun cancel() {
        require(status != Status.PAID) {
            "paid cannot be cancelled"
        }

        status = Status.CANCELLED
        touch()
    }

    private fun touch() {
        updatedAt = Instant.now()
    }
}
