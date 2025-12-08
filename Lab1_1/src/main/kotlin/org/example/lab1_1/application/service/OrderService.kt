package org.example.lab1_1.application.service

import org.example.lab1_1.domain.model.Order
import org.example.lab1_1.domain.model.OrderItem
import org.example.lab1_1.domain.model.OrderStatus
import java.util.UUID

interface OrderService {
    fun create(customerId: UUID, order: Order): Order
    fun get(id: UUID): Order
    fun getAll(): List<Order>
    fun getByOrderNumber(orderNumber: String): Order?
    fun update(orderId: UUID, customerId: UUID, update: Order): Order
    fun updateStatus(orderId: UUID, status: OrderStatus): Order
    fun delete(id: UUID)
}
