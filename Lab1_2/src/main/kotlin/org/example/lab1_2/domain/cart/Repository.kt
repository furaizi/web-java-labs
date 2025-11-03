package org.example.lab1_2.domain.cart

import java.util.UUID

interface CartRepository {
    fun nextId(): CartId =
        CartId(UUID.randomUUID())

    fun findById(id: CartId): Cart?
    fun save(aggregate: Cart): Cart
}