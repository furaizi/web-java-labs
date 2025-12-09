package org.example.lab1_1.security

import io.mockk.every
import io.mockk.verify
import org.example.lab1_1.application.dto.ProductDetailsDto
import org.example.lab1_1.application.dto.ProductStatusDto
import org.example.lab1_1.application.service.ProductService
import org.example.lab1_1.domain.common.PageResult
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest(
    properties = [
        "spring.security.api-key=test-key"
    ]
)
@AutoConfigureMockMvc
class ApiSecurityTest @Autowired constructor(
    private val mockMvc: MockMvc,
    @MockkBean private val productService: ProductService
) {

    private val sampleProduct = ProductDetailsDto(
        id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
        sku = "SKU-SEC",
        name = "Secure Nebula Dust",
        description = "Secured",
        price = BigDecimal("12.34"),
        currency = "USD",
        categoryId = null,
        status = ProductStatusDto.ACTIVE,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )

    @Test
    @WithMockUser(username = "cosmo-cat", roles = ["USER"])
    fun `rejects request without api key`() {
        mockMvc.perform(
            get("/api/v1/products")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `allows request with jwt and api key`() {
        every { productService.list(any(), any(), any(), any(), any(), any(), any(), any()) } returns PageResult(
            content = listOf(sampleProduct),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1
        )

        mockMvc.perform(
            get("/api/v1/products")
                .header("X-API-KEY", "test-key")
                .with(jwt().jwt { jwt -> jwt.subject("cosmo-cat") })
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(sampleProduct.id.toString()))
            .andExpect(jsonPath("$.content[0].name").value(sampleProduct.name))

        verify(exactly = 1) {
            productService.list(
                q = null,
                categoryId = null,
                status = null,
                minPrice = null,
                maxPrice = null,
                page = 0,
                size = 20,
                sort = emptyList()
            )
        }
    }
}
