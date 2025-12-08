package org.example.lab1_1.application.service

import org.example.lab1_1.domain.model.Customer
import org.example.lab1_1.domain.model.Order
import org.example.lab1_1.domain.model.OrderItem
import org.example.lab1_1.domain.model.OrderStatus
import org.example.lab1_1.domain.model.Product
import org.example.lab1_1.domain.repository.CustomerRepository
import org.example.lab1_1.domain.repository.OrderRepository
import org.example.lab1_1.domain.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
) : OrderService {

    override fun create(customerId: UUID, order: Order): Order {
        order.customer = resolveCustomer(customerId)
        rebuildItems(order, order.items)
        order.totalAmount = calculateTotal(order)
        return orderRepository.save(order)
    }

    @Transactional(readOnly = true)
    override fun get(id: UUID): Order =
        orderRepository.findByPublicId(id) ?: throw NoSuchElementException("Order $id not found")

    @Transactional(readOnly = true)
    override fun getAll(): List<Order> = orderRepository.findAll()

    @Transactional(readOnly = true)
    override fun getByOrderNumber(orderNumber: String): Order? = orderRepository.findByOrderNumber(orderNumber)

    override fun update(orderId: UUID, customerId: UUID, update: Order): Order {
        val existing = orderRepository.findByPublicId(orderId)
            ?: throw NoSuchElementException("Order $orderId not found")
        existing.customer = resolveCustomer(customerId)
        existing.status = update.status
        existing.currencyCode = update.currencyCode

        existing.items.clear()
        rebuildItems(existing, update.items)
        existing.totalAmount = calculateTotal(existing)
        return existing
    }

    override fun updateStatus(orderId: UUID, status: OrderStatus): Order {
        val order = orderRepository.findByPublicId(orderId)
            ?: throw NoSuchElementException("Order $orderId not found")
        order.status = status
        return order
    }

    override fun delete(id: UUID) {
        val order = orderRepository.findByPublicId(id) ?: throw NoSuchElementException("Order $id not found")
        orderRepository.delete(order)
    }

    private fun rebuildItems(order: Order, newItems: Collection<OrderItem>) {
        newItems.forEach { item ->
            item.order = order
            item.product = resolveProduct(item.product?.id, item.product?.publicId)
            order.items.add(item)
        }
    }

    private fun calculateTotal(order: Order): BigDecimal =
        order.items.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.unitPrice.multiply(BigDecimal(item.quantity))
        }

    private fun resolveCustomer(customerId: UUID): Customer =
        customerRepository.findByPublicId(customerId)
            ?: throw NoSuchElementException("Customer $customerId not found")

    private fun resolveProduct(productId: Long?, publicId: UUID?): Product {
        val resolvedById = productId?.let { id ->
            productRepository.findById(id).orElse(null)
        }
        if (resolvedById != null) return resolvedById
        val resolvedByPublic = publicId?.let { pid -> productRepository.findByPublicId(pid) }
        if (resolvedByPublic != null) return resolvedByPublic
        throw NoSuchElementException("Product reference not found")
    }
}
