package org.example.lab1_1.domain.repository

import org.example.lab1_1.domain.model.OrderItem
import org.example.lab1_1.domain.repository.projection.ProductSalesProjection
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
    fun existsByProductId(productId: Long): Boolean

    @Query(
        """
        select p.id as productId, p.name as productName, sum(oi.quantity) as totalQuantity
        from OrderItem oi
        join oi.product p
        group by p.id, p.name
        order by sum(oi.quantity) desc
        """
    )
    fun findTopProducts(pageable: Pageable): List<ProductSalesProjection>
}
