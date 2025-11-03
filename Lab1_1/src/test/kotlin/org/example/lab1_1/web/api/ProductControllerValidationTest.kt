package org.example.lab1_1.web.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jayway.jsonpath.JsonPath
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductDetailsDto
import org.example.lab1_1.application.dto.ProductPatchDto
import org.example.lab1_1.application.dto.ProductStatusDto
import org.example.lab1_1.application.service.ProductService
import org.example.lab1_1.domain.common.PageResult
import org.example.lab1_1.web.advice.GlobalErrorHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.skyscreamer.jsonassert.JSONAssert
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import io.mockk.justRun

class ProductControllerValidationTest {

    private val service = mockk<ProductService>()
    private val objectMapper: ObjectMapper = Jackson2ObjectMapperBuilder
        .json()
        .modulesToInstall(KotlinModule.Builder().build())
        .build<ObjectMapper>()
        .findAndRegisterModules()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(ProductController(service))
        .setControllerAdvice(GlobalErrorHandler())
        .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
        .build()

    @AfterEach
    fun tearDown() {
        clearMocks(service)
    }

    @Test
    fun `create returns 201 when payload valid`() {
        val request = ProductCreateDto(
            sku = "SKU-VALID",
            name = "Galactic star dust",
            description = "Premium interstellar dust",
            price = BigDecimal("42.50"),
            currency = "USD",
            categoryId = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"),
            status = ProductStatusDto.ACTIVE
        )
        val created = ProductDetailsDto(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            sku = request.sku,
            name = request.name,
            description = request.description,
            price = request.price.setScale(2),
            currency = request.currency,
            categoryId = request.categoryId,
            status = request.status,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z")
        )

        every { service.create(any()) } returns created

        val mvcResult = mockMvc.perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        mvcResult.response.status shouldBe 201
        mvcResult.response.getHeader("Location") shouldBe "/api/v1/products/${created.id}"
        val body = mvcResult.response.contentAsString
        JSONAssert.assertEquals(
            objectMapper.writeValueAsString(created),
            body,
            true
        )

        verify(exactly = 1) {
            service.create(
                withArg {
                    it.sku shouldBe request.sku
                    it.name shouldBe request.name
                    it.price shouldBe request.price
                    it.currency shouldBe request.currency
                    it.categoryId shouldBe request.categoryId
                    it.status shouldBe request.status
                }
            )
        }
        confirmVerified(service)
    }

    @Test
    fun `list forwards filters and returns page`() {
        val categoryId = UUID.fromString("12345678-1234-5678-9abc-def012345678")
        val item = ProductDetailsDto(
            id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            sku = "SKU-LIST",
            name = "Cosmic orbit pack",
            description = "Bundle",
            price = BigDecimal("19.99"),
            currency = "USD",
            categoryId = categoryId,
            status = ProductStatusDto.ACTIVE,
            createdAt = Instant.parse("2024-01-10T10:00:00Z"),
            updatedAt = Instant.parse("2024-01-10T10:00:00Z")
        )
        val pageResult = PageResult(
            content = listOf(item),
            page = 2,
            size = 5,
            totalElements = 12,
            totalPages = 3
        )

        every {
            service.list(
                q = "ice",
                categoryId = categoryId,
                status = ProductStatusDto.ACTIVE,
                minPrice = BigDecimal("10.00"),
                maxPrice = BigDecimal("25.00"),
                page = 2,
                size = 5,
                sort = listOf("name,desc", "price")
            )
        } returns pageResult

        val mvcResult = mockMvc.perform(
            get("/api/v1/products")
                .param("q", "ice")
                .param("category", categoryId.toString())
                .param("status", "ACTIVE")
                .param("minPrice", "10.00")
                .param("maxPrice", "25.00")
                .param("page", "2")
                .param("size", "5")
                .param("sort", "name,desc")
                .param("sort", "price")
        ).andReturn()

        mvcResult.response.status shouldBe 200
        JSONAssert.assertEquals(
            objectMapper.writeValueAsString(pageResult),
            mvcResult.response.contentAsString,
            true
        )

        verify(exactly = 1) {
            service.list(
                q = "ice",
                categoryId = categoryId,
                status = ProductStatusDto.ACTIVE,
                minPrice = BigDecimal("10.00"),
                maxPrice = BigDecimal("25.00"),
                page = 2,
                size = 5,
                sort = listOf("name,desc", "price")
            )
        }
        confirmVerified(service)
    }

    @Test
    fun `get returns 200 with body`() {
        val id = UUID.fromString("33333333-3333-3333-3333-333333333333")
        val dto = ProductDetailsDto(
            id = id,
            sku = "SKU-GET",
            name = "Cosmic ring",
            description = "Info",
            price = BigDecimal("55.00"),
            currency = "USD",
            categoryId = null,
            status = ProductStatusDto.DRAFT,
            createdAt = Instant.parse("2024-02-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-02-01T00:00:00Z")
        )

        every { service.get(id) } returns dto

        val mvcResult = mockMvc.perform(
            get("/api/v1/products/{id}", id)
        ).andReturn()

        mvcResult.response.status shouldBe 200
        JSONAssert.assertEquals(
            objectMapper.writeValueAsString(dto),
            mvcResult.response.contentAsString,
            true
        )

        verify(exactly = 1) { service.get(id) }
        confirmVerified(service)
    }

    @Test
    fun `replace returns 200 when payload valid`() {
        val id = UUID.fromString("44444444-4444-4444-4444-444444444444")
        val request = ProductCreateDto(
            sku = "SKU-REPLACE",
            name = "Cosmic nova",
            description = "Replace description",
            price = BigDecimal("77.70"),
            currency = "USD",
            categoryId = null,
            status = ProductStatusDto.ACTIVE
        )
        val replaced = ProductDetailsDto(
            id = id,
            sku = request.sku,
            name = request.name,
            description = request.description,
            price = request.price.setScale(2),
            currency = request.currency,
            categoryId = null,
            status = request.status,
            createdAt = Instant.parse("2024-03-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-03-01T00:00:00Z")
        )

        every { service.replace(id, any()) } returns replaced

        val mvcResult = mockMvc.perform(
            put("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        mvcResult.response.status shouldBe 200
        JSONAssert.assertEquals(
            objectMapper.writeValueAsString(replaced),
            mvcResult.response.contentAsString,
            true
        )

        verify(exactly = 1) {
            service.replace(
                id,
                withArg {
                    it.sku shouldBe request.sku
                    it.name shouldBe request.name
                    it.price shouldBe request.price
                    it.currency shouldBe request.currency
                }
            )
        }
        confirmVerified(service)
    }

    @Test
    fun `patch returns 200 when payload valid`() {
        val id = UUID.fromString("55555555-5555-5555-5555-555555555555")
        val request = ProductPatchDto(
            sku = "SKU-PATCH-NEW",
            name = "Cosmic orbit deluxe",
            description = "Updated description"
        )
        val updated = ProductDetailsDto(
            id = id,
            sku = "SKU-PATCH-NEW",
            name = "Cosmic orbit deluxe",
            description = "Updated description",
            price = BigDecimal("90.00"),
            currency = "USD",
            categoryId = null,
            status = ProductStatusDto.ACTIVE,
            createdAt = Instant.parse("2024-04-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-04-02T00:00:00Z")
        )

        every { service.patch(id, any()) } returns updated

        val mvcResult = mockMvc.perform(
            patch("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        mvcResult.response.status shouldBe 200
        JSONAssert.assertEquals(
            objectMapper.writeValueAsString(updated),
            mvcResult.response.contentAsString,
            true
        )

        verify(exactly = 1) {
            service.patch(
                id,
                withArg {
                    it.sku shouldBe request.sku
                    it.name shouldBe request.name
                    it.description shouldBe request.description
                }
            )
        }
        confirmVerified(service)
    }

    @Test
    fun `delete returns 204`() {
        val id = UUID.fromString("66666666-6666-6666-6666-666666666666")
        justRun { service.delete(id) }

        val mvcResult = mockMvc.perform(
            delete("/api/v1/products/{id}", id)
        ).andReturn()

        mvcResult.response.status shouldBe 204
        mvcResult.response.contentAsString shouldBe ""

        verify(exactly = 1) { service.delete(id) }
        confirmVerified(service)
    }

    @Test
    fun `create returns 400 when payload fails validation`() {
        val request = mapOf(
            "sku" to "invalid_sku",
            "name" to "Regular product name",
            "price" to 19,
            "currency" to "USD"
        )

        val mvcResult = mockMvc.perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        mvcResult.response.status shouldBe 400
        val body = mvcResult.response.contentAsString
        val errors = JsonPath.read<List<Map<String, Any?>>>(body, "$.errors")
        errors.shouldHaveSize(2)
        val fields = errors.mapNotNull { it["field"]?.toString() }
        fields shouldContainAll listOf("sku", "name")

        verify(exactly = 0) { service.create(any()) }
        confirmVerified(service)
    }

    @Test
    fun `patch returns 400 when optional fields violate validation`() {
        val id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val request = ProductPatchDto(
            sku = "SKU-VALID",
            name = "   ",
            price = BigDecimal("-5.00")
        )

        val mvcResult = mockMvc.perform(
            patch("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andReturn()

        mvcResult.response.status shouldBe 400
        val body = mvcResult.response.contentAsString
        val errors = JsonPath.read<List<Map<String, Any?>>>(body, "$.errors")
        errors.shouldHaveSize(2)
        val fields = errors.mapNotNull { it["field"]?.toString() }
        fields shouldContainAll listOf("name", "price")

        verify(exactly = 0) { service.patch(any(), any()) }
        confirmVerified(service)
    }
}
