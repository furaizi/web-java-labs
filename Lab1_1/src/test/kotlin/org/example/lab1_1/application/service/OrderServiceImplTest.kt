package org.example.lab1_1.application.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.lab1_1.domain.model.Category
import org.example.lab1_1.domain.model.Customer
import org.example.lab1_1.domain.model.Order
import org.example.lab1_1.domain.model.OrderItem
import org.example.lab1_1.domain.model.OrderStatus
import org.example.lab1_1.domain.model.Product
import org.example.lab1_1.domain.model.ProductStatus
import org.example.lab1_1.domain.repository.CustomerRepository
import org.example.lab1_1.domain.repository.OrderRepository
import org.example.lab1_1.domain.repository.ProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

class OrderServiceImplTest {

    private val orderRepository: OrderRepository = mockk(relaxed = true)
    private val customerRepository: CustomerRepository = mockk(relaxed = true)
    private val productRepository: ProductRepository = mockk(relaxed = true)
    private val service = OrderServiceImpl(orderRepository, customerRepository, productRepository)

    @Test
    fun `create resolves customer and product and calculates total`() {
        val customer = Customer(id = 1L, publicId = UUID.randomUUID(), email = "a@a.com", fullName = "A")
        val product = sampleProduct()
        val order = Order(
            customer = null,
            orderNumber = "ORD-1",
            status = OrderStatus.DRAFT,
            currencyCode = "USD",
            totalAmount = BigDecimal.ZERO
        )
        order.items.add(
            OrderItem(
                order = order,
                product = product,
                quantity = 2,
                unitPrice = BigDecimal("15.00"),
                currencyCode = "USD"
            )
        )

        every { customerRepository.findByPublicId(customer.publicId) } returns customer
        every { productRepository.findById(product.id!!) } returns Optional.of(product)
        every { orderRepository.save(order) } returns order

        val result = service.create(customer.publicId, order)

        result.customer shouldBe customer
        result.totalAmount shouldBe BigDecimal("30.00")
        result.items.shouldHaveSize(1)
        verify(exactly = 1) { orderRepository.save(order) }
    }

    @Test
    fun `update replaces items and recomputes total`() {
        val customer = Customer(id = 2L, publicId = UUID.randomUUID(), email = "b@b.com", fullName = "B")
        val product = sampleProduct()
        val existing = Order(
            id = 10L,
            publicId = UUID.randomUUID(),
            customer = customer,
            orderNumber = "ORD-2",
            status = OrderStatus.DRAFT,
            currencyCode = "USD",
            totalAmount = BigDecimal.ZERO
        )
        val update = Order(
            customer = customer,
            orderNumber = "ORD-2",
            status = OrderStatus.PAID,
            currencyCode = "USD",
            totalAmount = BigDecimal.ZERO
        ).apply {
            items.add(
                OrderItem(
                    order = this,
                    product = product,
                    quantity = 3,
                    unitPrice = BigDecimal("5.00"),
                    currencyCode = "USD"
                )
            )
        }

        every { orderRepository.findByPublicId(existing.publicId) } returns existing
        every { customerRepository.findByPublicId(customer.publicId) } returns customer
        every { productRepository.findById(product.id!!) } returns Optional.of(product)
        every { orderRepository.save(existing) } returns existing

        val result = service.update(existing.publicId, customer.publicId, update)

        result.items.shouldHaveSize(1)
        result.totalAmount shouldBe BigDecimal("15.00")
        result.status shouldBe OrderStatus.PAID
    }

    @Test
    fun `updateStatus sets status`() {
        val existing = Order(
            id = 11L,
            publicId = UUID.randomUUID(),
            customer = null,
            orderNumber = "ORD-3",
            status = OrderStatus.DRAFT,
            currencyCode = "USD",
            totalAmount = BigDecimal.ZERO
        )
        every { orderRepository.findByPublicId(existing.publicId) } returns existing

        val updated = service.updateStatus(existing.publicId, OrderStatus.CANCELLED)

        updated.status shouldBe OrderStatus.CANCELLED
    }

    @Test
    fun `get and getAll and getByOrderNumber delegate to repository`() {
        val order = sampleOrder()
        every { orderRepository.findByPublicId(order.publicId) } returns order
        every { orderRepository.findAll() } returns listOf(order)
        every { orderRepository.findByOrderNumber(order.orderNumber) } returns order

        service.get(order.publicId) shouldBe order
        service.getAll() shouldHaveSize 1
        service.getByOrderNumber(order.orderNumber) shouldBe order
    }

    @Test
    fun `delete removes resolved order`() {
        val order = sampleOrder()
        every { orderRepository.findByPublicId(order.publicId) } returns order

        service.delete(order.publicId)

        verify(exactly = 1) { orderRepository.delete(order) }
    }

    @Test
    fun `create resolves product by publicId when id is null`() {
        val customer = Customer(id = 3L, publicId = UUID.randomUUID(), email = "c@c.com", fullName = "C")
        val productPublicId = UUID.randomUUID()
        val product = sampleProduct().apply { id = null; publicId = productPublicId }
        val persistedProduct = sampleProduct()
        val order = Order(
            customer = null,
            orderNumber = "ORD-4",
            status = OrderStatus.DRAFT,
            currencyCode = "USD",
            totalAmount = BigDecimal.ZERO
        ).apply {
            items.add(
                OrderItem(
                    order = this,
                    product = product,
                    quantity = 1,
                    unitPrice = BigDecimal("7.00"),
                    currencyCode = "USD"
                )
            )
        }

        every { customerRepository.findByPublicId(customer.publicId) } returns customer
        every { productRepository.findByPublicId(productPublicId) } returns persistedProduct
        every { orderRepository.save(order) } returns order

        val saved = service.create(customer.publicId, order)

        saved.items.first().product shouldBe persistedProduct
    }

    private fun sampleOrder(): Order = Order(
        id = 12L,
        publicId = UUID.randomUUID(),
        customer = null,
        orderNumber = "ORD-9",
        status = OrderStatus.DRAFT,
        currencyCode = "USD",
        totalAmount = BigDecimal.ZERO
    )

    private fun sampleProduct(): Product = Product(
        id = 5L,
        publicId = UUID.randomUUID(),
        category = Category(id = 1L, publicId = UUID.randomUUID(), name = "Cat"),
        sku = "SKU-X",
        name = "X",
        priceAmount = BigDecimal("15.00"),
        currencyCode = "USD",
        stockQuantity = 10,
        status = ProductStatus.ACTIVE
    )
}
