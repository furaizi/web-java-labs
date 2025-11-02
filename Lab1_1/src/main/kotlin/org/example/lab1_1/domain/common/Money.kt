package org.example.lab1_1.domain.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {

    init {
        require(amount.scale() <= 2) {
            "scale > 2 not allowed"
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "currency mismatch"
        }

        return copy(
            amount = amount.add(other.amount)
        )
    }

    operator fun times(qty: Quantity) = copy(
        amount = amount.multiply(BigDecimal(qty.value))
    )


    fun nonNegative(): Money {
        require(amount >= BigDecimal.ZERO) {
            "money < 0"
        }

        return this
    }

    fun normalized(scale: Int = 2) = copy(
        amount = amount.setScale(scale, RoundingMode.HALF_UP)
    )

    companion object {
        fun zero(cur: Currency) = Money(
            BigDecimal.ZERO.setScale(2),
            cur
        )
    }
}
