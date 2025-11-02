package org.example.lab1_1.domain.common

@JvmInline
value class Quantity(val value: Int) {

    init {
        require(value > 0) {
            "quantity must be > 0"
        }
    }

    operator fun plus(other: Quantity) = Quantity(value + other.value)
    operator fun minus(other: Quantity) = Quantity(value - other.value)
}