package org.example.lab1_1.domain.order

import java.util.UUID

interface OrderRepository {
    fun nextId(): OrderId =
        OrderId(UUID.randomUUID())

    fun findById(id: OrderId): Order?
    fun save(aggregate: Order): Order
}