package org.example.lab1_1.integration

import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductPatchDto
import org.example.lab1_1.application.dto.ProductStatusDto
import org.example.lab1_1.application.service.CategoryService
import org.example.lab1_1.application.service.OrderService
import org.example.lab1_1.application.service.ProductService
import org.example.lab1_1.domain.model.Category
import org.example.lab1_1.domain.model.Customer
import org.example.lab1_1.domain.model.Order
import org.example.lab1_1.domain.model.OrderItem
import org.example.lab1_1.domain.model.OrderStatus
import org.example.lab1_1.domain.model.ProductStatus
import org.example.lab1_1.domain.repository.CustomerRepository
import org.example.lab1_1.domain.repository.ProductRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tc")
@Testcontainers
class DatabaseIntegrationTest @Autowired constructor(
    private val productService: ProductService,
    private val categoryService: CategoryService,
    private val orderService: OrderService,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository
) {

    @Test
    fun `product CRUD with filtering and projection`() {
        val category = categoryService.create(Category(name = "Nebulae"))

        val created = productService.create(
            ProductCreateDto(
                sku = "SKU-TC-1",
                name = "Nebula Dust",
                description = "Space grade dust",
                price = BigDecimal("10.50"),
                currency = "USD",
                categoryId = category.publicId,
                status = ProductStatusDto.ACTIVE
            )
        )

        val fetched = productService.get(created.id)
        assertEquals("Nebula Dust", fetched.name)
        assertEquals(category.publicId, fetched.categoryId)

        val patched = productService.patch(
            created.id,
            ProductPatchDto(
                name = "Updated Dust",
                price = BigDecimal("12.00"),
                status = ProductStatusDto.ARCHIVED
            )
        )
        assertEquals("Updated Dust", patched.name)
        assertEquals(ProductStatusDto.ARCHIVED, patched.status)

        val page = productService.list(
            q = "dust",
            categoryId = category.publicId,
            status = ProductStatusDto.ARCHIVED,
            minPrice = BigDecimal("11.00"),
            maxPrice = BigDecimal("15.00"),
            page = 0,
            size = 5,
            sort = listOf("name,asc")
        )
        assertEquals(1, page.totalElements)
        assertEquals(patched.id, page.content.first().id)
    }

    @Test
    fun `order service persists totals and natural id`() {
        val category = categoryService.create(Category(name = "Galaxies"))
        val productDto = productService.create(
            ProductCreateDto(
                sku = "SKU-TC-2",
                name = "Galaxy Paint",
                description = "Dark matter paint",
                price = BigDecimal("20.00"),
                currency = "USD",
                categoryId = category.publicId,
                status = ProductStatusDto.ACTIVE
            )
        )
        val productEntity = productRepository.findByPublicId(productDto.id)
            ?: error("Product entity not found")

        val customer = customerRepository.save(
            Customer(
                email = "space.cadet@example.com",
                fullName = "Space Cadet"
            )
        )

        val order = Order(
            customer = null,
            orderNumber = "ORD-42",
            status = OrderStatus.DRAFT,
            currencyCode = "USD",
            totalAmount = BigDecimal.ZERO
        )
        order.items.add(
            OrderItem(
                order = order,
                product = productEntity,
                quantity = 2,
                unitPrice = BigDecimal("20.00"),
                currencyCode = "USD"
            )
        )

        val persisted = orderService.create(customer.publicId, order)
        assertEquals(0, persisted.totalAmount.compareTo(BigDecimal("40.00")))
        assertEquals(1, persisted.items.size)
        assertNotNull(orderService.getByOrderNumber("ORD-42"))

        val updated = orderService.updateStatus(persisted.publicId, OrderStatus.CANCELLED)
        assertEquals(OrderStatus.CANCELLED, updated.status)

        val top = productService.findTopSelling(1).first()
        assertEquals(productEntity.id, top.productId)
        assertEquals("Galaxy Paint", top.productName)
        assertEquals(2L, top.totalQuantity)
    }

    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("cosmocats")
            withUsername("postgres")
            withPassword("postgres")
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
            registry.add("spring.liquibase.enabled") { true }
            registry.add("spring.liquibase.change-log") { "classpath:db/changelog/db.changelog-master.yml" }
        }
    }
}
