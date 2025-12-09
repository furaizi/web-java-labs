package org.example.lab1_1.domain.repository

import org.example.lab1_1.domain.model.Order
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByPublicId(publicId: UUID): Order?
}
