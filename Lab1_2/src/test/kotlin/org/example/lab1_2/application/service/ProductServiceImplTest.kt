package org.example.lab1_2.application.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.example.lab1_2.application.dto.ProductCreateDto
import org.example.lab1_2.application.dto.ProductPatchDto
import org.example.lab1_2.application.dto.ProductStatusDto
import org.example.lab1_2.domain.common.Money
import org.example.lab1_2.domain.common.PageRequest
import org.example.lab1_2.domain.common.PageResult
import org.example.lab1_2.domain.common.Sort
import org.example.lab1_2.domain.common.Direction
import org.example.lab1_2.domain.product.CategoryId
import org.example.lab1_2.domain.product.Product
import org.example.lab1_2.domain.product.Product.Status
import org.example.lab1_2.domain.product.ProductId
import org.example.lab1_2.domain.product.ProductRepository
import org.example.lab1_2.domain.product.ProductSearch
import org.example.lab1_2.domain.product.Sku
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID

class ProductServiceImplTest {

    @Test
    fun `create stores aggregate and returns dto`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val nextId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val categoryId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val dto = ProductCreateDto(
            sku = "SKU-123",
            name = "Red Planet Dust",
            description = "Fine powder from Mars",
            price = BigDecimal("19.75"),
            currency = "USD",
            categoryId = categoryId,
            status = ProductStatusDto.ACTIVE
        )
        val existingSkuHolder = domainProduct(id = UUID.randomUUID(), sku = dto.sku)
        val persisted = domainProduct(
            id = nextId,
            sku = dto.sku,
            name = dto.name,
            description = dto.description,
            price = dto.price,
            currency = Currency.getInstance(dto.currency),
            categoryId = categoryId,
            status = Status.ACTIVE,
            createdAt = Instant.parse("2024-01-05T10:15:30Z")
        )
        val savedSlot = slot<Product>()

        every { repo.findBySku(Sku(dto.sku)) } returns existingSkuHolder
        every { repo.nextId() } returns ProductId(nextId)
        every { repo.save(capture(savedSlot)) } returns persisted

        val result = service.create(dto)

        result.id shouldBe nextId
        result.sku shouldBe dto.sku
        result.name shouldBe dto.name
        result.description shouldBe dto.description
        result.price shouldBe dto.price.setScale(2)
        result.currency shouldBe dto.currency
        result.categoryId shouldBe categoryId
        result.status shouldBe ProductStatusDto.ACTIVE
        result.createdAt shouldBe persisted.createdAt
        result.updatedAt shouldBe persisted.updatedAt

        savedSlot.captured.id.value shouldBe nextId
        savedSlot.captured.sku.value shouldBe dto.sku
        savedSlot.captured.name shouldBe dto.name
        savedSlot.captured.description shouldBe dto.description
        savedSlot.captured.price.amount shouldBe dto.price.setScale(2)
        savedSlot.captured.currency shouldBe Currency.getInstance(dto.currency)
        savedSlot.captured.categoryId?.value shouldBe categoryId
        savedSlot.captured.status shouldBe Status.ACTIVE

        verify(exactly = 1) { repo.findBySku(Sku(dto.sku)) }
        verify(exactly = 1) { repo.nextId() }
        verify(exactly = 1) { repo.save(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `create fails when sku is not present in repository`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val dto = ProductCreateDto(
            sku = "SKU-321",
            name = "Lunar Rock",
            description = null,
            price = BigDecimal("5.50"),
            currency = "USD"
        )

        every { repo.findBySku(Sku(dto.sku)) } returns null

        shouldThrowExactly<IllegalArgumentException> {
            service.create(dto)
        }

        verify(exactly = 1) { repo.findBySku(Sku(dto.sku)) }
        verify(exactly = 0) { repo.nextId() }
        verify(exactly = 0) { repo.save(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `get returns dto when aggregate exists`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
        val product = domainProduct(
            id = id,
            sku = "SKU-999",
            name = "Titan Atmosphere",
            description = "Smog collected from Titan",
            price = BigDecimal("45.10"),
            currency = Currency.getInstance("EUR"),
            status = Status.ARCHIVED
        )

        every { repo.findById(ProductId(id)) } returns product

        val dto = service.get(id)

        dto.id shouldBe id
        dto.sku shouldBe "SKU-999"
        dto.name shouldBe "Titan Atmosphere"
        dto.description shouldBe "Smog collected from Titan"
        dto.price shouldBe BigDecimal("45.10")
        dto.currency shouldBe "EUR"
        dto.status shouldBe ProductStatusDto.ARCHIVED

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        confirmVerified(repo)
    }

    @Test
    fun `get throws when product missing`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")

        every { repo.findById(ProductId(id)) } returns null

        shouldThrowExactly<NoSuchElementException> {
            service.get(id)
        }

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        confirmVerified(repo)
    }

    @Test
    fun `list maps filter sort and page result`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val categoryId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
        val product = domainProduct(
            id = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            sku = "SKU-LIST",
            name = "Europa Ice",
            description = "Fresh ice from Europa",
            price = BigDecimal("12.34"),
            currency = Currency.getInstance("USD"),
            status = Status.ACTIVE,
            categoryId = categoryId
        )
        val filterSlot = slot<ProductSearch>()
        val pageSlot = slot<PageRequest>()
        val sortSlot = slot<List<Sort>>()

        every {
            repo.search(capture(filterSlot), capture(pageSlot), capture(sortSlot))
        } returns PageResult(
            content = listOf(product),
            page = 2,
            size = 5,
            totalElements = 14,
            totalPages = 3
        )

        val result = service.list(
            q = "ice",
            categoryId = categoryId,
            status = ProductStatusDto.ACTIVE,
            minPrice = BigDecimal("10.00"),
            maxPrice = BigDecimal("20.00"),
            page = 2,
            size = 5,
            sort = listOf("name,desc", " price ,ASC", "unknown", "sku")
        )

        filterSlot.captured shouldBe ProductSearch(
            q = "ice",
            categoryId = categoryId,
            status = Status.ACTIVE,
            minPrice = BigDecimal("10.00"),
            maxPrice = BigDecimal("20.00")
        )
        pageSlot.captured shouldBe PageRequest(2, 5)
        sortSlot.captured.shouldContainExactly(
            Sort(field = "name", direction = Direction.DESC),
            Sort(field = "price", direction = Direction.ASC),
            Sort(field = "sku", direction = Direction.ASC)
        )

        result.content shouldHaveSize 1
        val row = result.content.first()
        row.id shouldBe product.id.value
        row.sku shouldBe "SKU-LIST"
        row.name shouldBe "Europa Ice"
        row.description shouldBe "Fresh ice from Europa"
        row.price shouldBe BigDecimal("12.34")
        row.currency shouldBe "USD"
        row.categoryId shouldBe categoryId
        row.status shouldBe ProductStatusDto.ACTIVE
        result.page shouldBe 2
        result.size shouldBe 5
        result.totalElements shouldBe 14
        result.totalPages shouldBe 3

        verify(exactly = 1) { repo.search(any(), any(), any()) }
        confirmVerified(repo)
    }

    @Test
    fun `replace recreates aggregate when sku unchanged`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("11111111-2222-3333-4444-555555555555")
        val existing = domainProduct(id = id, sku = "SKU-OLD", name = "Old Name")
        val dto = ProductCreateDto(
            sku = "SKU-OLD",
            name = "New Name",
            description = "Updated description",
            price = BigDecimal("77.70"),
            currency = "USD",
            categoryId = UUID.fromString("22222222-3333-4444-5555-666666666666"),
            status = ProductStatusDto.ARCHIVED
        )
        val savedSlot = slot<Product>()
        val persisted = domainProduct(
            id = id,
            sku = dto.sku,
            name = dto.name,
            description = dto.description,
            price = dto.price,
            currency = Currency.getInstance("USD"),
            categoryId = dto.categoryId,
            status = Status.ARCHIVED
        )

        every { repo.findById(ProductId(id)) } returns existing
        every { repo.save(capture(savedSlot)) } returns persisted

        val result = service.replace(id, dto)

        result.id shouldBe id
        result.name shouldBe "New Name"
        result.description shouldBe "Updated description"
        result.status shouldBe ProductStatusDto.ARCHIVED

        savedSlot.captured.id.value shouldBe id
        savedSlot.captured.sku.value shouldBe dto.sku
        savedSlot.captured.name shouldBe "New Name"
        savedSlot.captured.status shouldBe Status.ARCHIVED

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        verify(exactly = 1) { repo.save(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `replace throws when aggregate missing`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("99999999-8888-7777-6666-555555555555")
        val dto = ProductCreateDto(
            sku = "SKU-MISS",
            name = "Missing",
            price = BigDecimal("10.00"),
            currency = "USD"
        )

        every { repo.findById(ProductId(id)) } returns null

        shouldThrowExactly<NoSuchElementException> {
            service.replace(id, dto)
        }

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        confirmVerified(repo)
    }

    @Test
    fun `replace with new sku calls unique check and succeeds when sku exists`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("33333333-4444-5555-6666-777777777777")
        val existing = domainProduct(id = id, sku = "SKU-OLD")
        val dto = ProductCreateDto(
            sku = "SKU-NEW",
            name = "Brand New",
            description = null,
            price = BigDecimal("30.00"),
            currency = "USD"
        )
        val other = domainProduct(id = UUID.randomUUID(), sku = dto.sku)
        val persisted = domainProduct(id = id, sku = dto.sku, name = dto.name, price = dto.price)

        every { repo.findById(ProductId(id)) } returns existing
        every { repo.findBySku(Sku(dto.sku)) } returns other
        every { repo.save(any()) } returns persisted

        val result = service.replace(id, dto)

        result.sku shouldBe dto.sku
        result.name shouldBe "Brand New"

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        verify(exactly = 1) { repo.findBySku(Sku(dto.sku)) }
        verify(exactly = 1) { repo.save(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `replace fails when new sku absent in repository`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("12121212-3434-5656-7878-909090909090")
        val existing = domainProduct(id = id, sku = "SKU-OLD")
        val dto = ProductCreateDto(
            sku = "SKU-NEW",
            name = "Updated",
            price = BigDecimal("11.11"),
            currency = "USD"
        )

        every { repo.findById(ProductId(id)) } returns existing
        every { repo.findBySku(Sku(dto.sku)) } returns null

        shouldThrowExactly<IllegalArgumentException> {
            service.replace(id, dto)
        }

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        verify(exactly = 1) { repo.findBySku(Sku(dto.sku)) }
        verify(exactly = 0) { repo.save(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `patch mutates existing product and saves result`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("abababab-abab-abab-abab-abababababab")
        val originalUpdatedAt = Instant.parse("2024-04-01T00:00:00Z")
        val existing = domainProduct(
            id = id,
            sku = "SKU-PATCH",
            name = "Initial Name",
            description = "Initial Desc",
            price = BigDecimal("40.00"),
            status = Status.DRAFT,
            createdAt = Instant.parse("2024-03-01T00:00:00Z"),
            updatedAt = originalUpdatedAt
        )
        val dto = ProductPatchDto(
            sku = "SKU-PATCH-NEW",
            name = "  Updated Name  ",
            description = "Updated Desc",
            price = BigDecimal("50.00"),
            categoryId = UUID.fromString("abababab-abab-abab-abab-000000000000"),
            status = ProductStatusDto.ACTIVE
        )
        val other = domainProduct(id = UUID.randomUUID(), sku = dto.sku!!)

        every { repo.findById(ProductId(id)) } returns existing
        every { repo.findBySku(Sku(dto.sku!!)) } returns other
        every { repo.save(existing) } returns existing

        val result = service.patch(id, dto)

        result.id shouldBe id
        result.sku shouldBe "SKU-PATCH-NEW"
        result.name shouldBe "Updated Name"
        result.description shouldBe "Updated Desc"
        result.price shouldBe BigDecimal("50.00")
        result.status shouldBe ProductStatusDto.ACTIVE
        result.categoryId shouldBe dto.categoryId
        result.updatedAt shouldNotBe originalUpdatedAt

        existing.sku.value shouldBe "SKU-PATCH-NEW"
        existing.name shouldBe "Updated Name"
        existing.description shouldBe "Updated Desc"
        existing.price.amount shouldBe BigDecimal("50.00").setScale(2)
        existing.status shouldBe Status.ACTIVE
        existing.categoryId?.value shouldBe dto.categoryId

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        verify(exactly = 1) { repo.findBySku(Sku(dto.sku!!)) }
        verify(exactly = 1) { repo.save(existing) }
        confirmVerified(repo)
    }

    @Test
    fun `patch throws when product missing`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("cdcdcdcd-cdcd-cdcd-cdcd-cdcdcdcdcdcd")
        val dto = ProductPatchDto(name = "Any")

        every { repo.findById(ProductId(id)) } returns null

        shouldThrowExactly<NoSuchElementException> {
            service.patch(id, dto)
        }

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        confirmVerified(repo)
    }

    @Test
    fun `patch fails when sku lookup returns null`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("efefefef-efef-efef-efef-efefefefefef")
        val existing = domainProduct(id = id, sku = "SKU-OLD")
        val dto = ProductPatchDto(sku = "SKU-NEW")

        every { repo.findById(ProductId(id)) } returns existing
        every { repo.findBySku(Sku("SKU-NEW")) } returns null

        shouldThrowExactly<IllegalArgumentException> {
            service.patch(id, dto)
        }

        verify(exactly = 1) { repo.findById(ProductId(id)) }
        verify(exactly = 1) { repo.findBySku(Sku("SKU-NEW")) }
        verify(exactly = 0) { repo.save(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `delete removes aggregate`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("45454545-4545-4545-4545-454545454545")

        every { repo.delete(ProductId(id)) } returns true

        service.delete(id)

        verify(exactly = 1) { repo.delete(ProductId(id)) }
        confirmVerified(repo)
    }

    @Test
    fun `delete throws when repo reports missing`() {
        val repo = mockk<ProductRepository>()
        val service = ProductServiceImpl(repo)
        val id = UUID.fromString("56565656-5656-5656-5656-565656565656")

        every { repo.delete(ProductId(id)) } returns false

        shouldThrowExactly<NoSuchElementException> {
            service.delete(id)
        }

        verify(exactly = 1) { repo.delete(ProductId(id)) }
        confirmVerified(repo)
    }

    private fun domainProduct(
        id: UUID = UUID.randomUUID(),
        sku: String = "SKU-DEFAULT",
        name: String = "Sample",
        description: String? = "Sample description",
        price: BigDecimal = BigDecimal("42.00"),
        currency: Currency = Currency.getInstance("USD"),
        categoryId: UUID? = UUID.randomUUID(),
        status: Status = Status.DRAFT,
        createdAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt: Instant = createdAt
    ): Product {
        return Product(
            id = ProductId(id),
            sku = Sku(sku),
            name = name,
            price = Money(price.setScale(2), currency),
            currency = currency,
            categoryId = categoryId?.let(::CategoryId),
            description = description,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
