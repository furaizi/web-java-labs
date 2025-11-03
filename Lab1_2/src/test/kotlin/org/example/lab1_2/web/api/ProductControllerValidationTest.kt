package org.example.lab1_2.web.api

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
import org.example.lab1_2.application.dto.ProductCreateDto
import org.example.lab1_2.application.dto.ProductDetailsDto
import org.example.lab1_2.application.dto.ProductPatchDto
import org.example.lab1_2.application.dto.ProductStatusDto
import org.example.lab1_2.application.service.ProductService
import org.example.lab1_2.web.advice.GlobalErrorHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.skyscreamer.jsonassert.JSONAssert
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

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
