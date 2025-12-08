package org.example.lab1_1.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductPatchDto
import org.example.lab1_1.application.dto.ProductStatusDto
import org.example.lab1_1.domain.common.PageResult
import org.example.lab1_1.domain.model.Category
import org.example.lab1_1.domain.model.Product
import org.example.lab1_1.domain.model.ProductStatus
import org.example.lab1_1.domain.repository.CategoryRepository
import org.example.lab1_1.domain.repository.OrderItemRepository
import org.example.lab1_1.domain.repository.ProductRepository
import org.example.lab1_1.domain.repository.projection.ProductSalesProjection
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

class ProductServiceImplTest {

    private val productRepository: ProductRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val orderItemRepository: OrderItemRepository = mockk()
    private val service = ProductServiceImpl(productRepository, categoryRepository, orderItemRepository)

    @Test
    fun `create maps dto to entity and returns details`() {
        val categoryId = UUID.randomUUID()
        val category = Category(publicId = categoryId, name = "Stars")
        val saved = sampleProduct(category = category).apply {
            createdAt = OffsetDateTime.parse("2024-10-01T12:00:00Z")
            updatedAt = createdAt
            status = ProductStatus.ACTIVE
            priceAmount = BigDecimal("10.50")
        }
        val savedSlot = slot<Product>()
        val dto = ProductCreateDto(
            sku = "SKU-1",
            name = "Nebula Dust",
            description = "Space dust",
            price = BigDecimal("10.50"),
            currency = "USD",
            categoryId = categoryId,
            status = ProductStatusDto.ACTIVE
        )

        every { categoryRepository.findByPublicId(categoryId) } returns category
        every { productRepository.save(capture(savedSlot)) } returns saved

        val result = service.create(dto)

        result.id shouldBe saved.publicId
        result.name shouldBe dto.name
        result.categoryId shouldBe categoryId
        result.status shouldBe ProductStatusDto.ACTIVE
        result.price shouldBe dto.price
        result.currency shouldBe dto.currency
        result.createdAt shouldBe saved.createdAt!!.toInstant()
        result.updatedAt shouldBe saved.updatedAt!!.toInstant()

        savedSlot.captured.category shouldBe category
        savedSlot.captured.status shouldBe ProductStatus.ACTIVE

        verify(exactly = 1) { categoryRepository.findByPublicId(categoryId) }
        verify(exactly = 1) { productRepository.save(any()) }
    }

    @Test
    fun `get returns mapped dto`() {
        val product = sampleProduct().apply {
            createdAt = OffsetDateTime.parse("2024-10-01T12:00:00Z")
            updatedAt = createdAt
        }
        every { productRepository.findByPublicId(product.publicId) } returns product

        val result = service.get(product.publicId)

        result.id shouldBe product.publicId
        result.name shouldBe product.name
        result.sku shouldBe product.sku
        result.createdAt shouldBe product.createdAt!!.toInstant()
    }

    @Test
    fun `replace updates all fields`() {
        val categoryId = UUID.randomUUID()
        val oldCategory = Category(publicId = UUID.randomUUID(), name = "Old")
        val newCategory = Category(publicId = categoryId, name = "New")
        val existing = sampleProduct(category = oldCategory)
        val dto = ProductCreateDto(
            sku = "SKU-99",
            name = "Galaxy Blend",
            description = "Updated description",
            price = BigDecimal("42.00"),
            currency = "EUR",
            categoryId = categoryId,
            status = ProductStatusDto.ARCHIVED
        )
        every { productRepository.findByPublicId(existing.publicId) } returns existing
        every { categoryRepository.findByPublicId(categoryId) } returns newCategory
        every { productRepository.save(existing) } returns existing

        val result = service.replace(existing.publicId, dto)

        result.name shouldBe dto.name
        existing.category shouldBe newCategory
        existing.status shouldBe ProductStatus.ARCHIVED

        verify(exactly = 1) { productRepository.save(existing) }
    }

    @Test
    fun `patch updates only provided fields`() {
        val existing = sampleProduct()
        val patch = ProductPatchDto(
            name = "Repainted",
            price = BigDecimal("25.00")
        )
        every { productRepository.findByPublicId(existing.publicId) } returns existing
        every { productRepository.save(existing) } returns existing

        val result = service.patch(existing.publicId, patch)

        result.name shouldBe "Repainted"
        existing.priceAmount shouldBe patch.price
        verify(exactly = 1) { productRepository.save(existing) }
    }

    @Test
    fun `list converts spring page to PageResult`() {
        val product = sampleProduct()
        every {
            productRepository.findAll(any(), any<PageRequest>())
        } returns PageImpl(listOf(product), PageRequest.of(0, 10), 1)

        val result: PageResult<*> = service.list(
            q = "nebula",
            categoryId = null,
            status = null,
            minPrice = null,
            maxPrice = null,
            page = 0,
            size = 10,
            sort = listOf("name,asc")
        )

        result.totalElements shouldBe 1
        val first = result.content.first()
        if (first is org.example.lab1_1.application.dto.ProductDetailsDto) {
            first.id shouldBe product.publicId
        }
    }

    @Test
    fun `findTopSelling delegates to repository`() {
        val projection = mockk<ProductSalesProjection>()
        every { orderItemRepository.findTopProducts(PageRequest.of(0, 5)) } returns listOf(projection)

        val result = service.findTopSelling(5)

        result shouldBe listOf(projection)
    }

    private fun sampleProduct(category: Category = Category(publicId = UUID.randomUUID(), name = "Stars")): Product =
        Product(
            id = 1L,
            publicId = UUID.randomUUID(),
            category = category,
            sku = "SKU-1",
            name = "Nebula Dust",
            description = "Space dust",
            priceAmount = BigDecimal("10.00"),
            currencyCode = "USD",
            stockQuantity = 5,
            status = ProductStatus.ACTIVE
        )
}
