package org.example.lab1_2.application.service

import org.example.lab1_2.application.dto.*
import org.example.lab1_2.application.mapper.ProductFactory
import org.example.lab1_2.application.mapper.applyPatch
import org.example.lab1_2.application.mapper.toDto
import org.example.lab1_2.domain.common.Direction
import org.example.lab1_2.domain.common.PageRequest
import org.example.lab1_2.domain.common.PageResult
import org.example.lab1_2.domain.common.Sort
import org.example.lab1_2.domain.product.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class ProductServiceImpl(
    private val repo: ProductRepository
) : ProductService {

    override fun create(dto: ProductCreateDto): ProductDetailsDto {
        requireUniqueSku(dto.sku)
        val product = ProductFactory.fromCreate(dto, repo.nextId().value)
        return repo.save(product).toDto()
    }

    override fun get(id: UUID): ProductDetailsDto =
        repo.findById(ProductId(id))?.toDto()
            ?: throw NoSuchElementException("Product $id not found")

    override fun list(
        q: String?,
        categoryId: UUID?,
        status: ProductStatusDto?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        page: Int,
        size: Int,
        sort: List<String>
    ): PageResult<ProductDetailsDto> {
        val filter = ProductSearch(
            q = q,
            categoryId = categoryId,
            status = status?.let {
                when (it) {
                    ProductStatusDto.DRAFT    -> Product.Status.DRAFT
                    ProductStatusDto.ACTIVE   -> Product.Status.ACTIVE
                    ProductStatusDto.ARCHIVED -> Product.Status.ARCHIVED
                }
            },
            minPrice = minPrice,
            maxPrice = maxPrice
        )

        val sortSpec = sort.mapNotNull { token ->
            val (field, dirRaw) = token.split(',', limit = 2)
                .let {
                    it[0].trim() to
                    it.getOrNull(1)
                        ?.trim()
                        ?.lowercase()
                }
            val fieldOk = when (field) {
                "name", "price", "createdAt", "updatedAt", "sku" -> field
                else -> return@mapNotNull null
            }
            Sort(
                field = fieldOk,
                direction = if (dirRaw == "desc")
                        Direction.DESC
                    else
                        Direction.ASC)
        }

        val pageRes: PageResult<Product> = repo.search(
            filter = filter,
            page = PageRequest(page, size),
            sort = sortSpec
        )

        return PageResult(
            content = pageRes.content.map { it.toDto() },
            page = pageRes.page,
            size = pageRes.size,
            totalElements = pageRes.totalElements,
            totalPages = pageRes.totalPages
        )
    }

    override fun replace(id: UUID, dto: ProductCreateDto): ProductDetailsDto {
        val existing = repo.findById(ProductId(id))
            ?: throw NoSuchElementException("Product $id not found")

        if (existing.sku.value != dto.sku)
            requireUniqueSku(dto.sku)

        val replaced = ProductFactory.fromCreate(dto, id)
        return repo.save(replaced).toDto()
    }

    override fun patch(id: UUID, dto: ProductPatchDto): ProductDetailsDto {
        val existing = repo.findById(ProductId(id))
            ?: throw NoSuchElementException("Product $id not found")

        dto.sku
            ?.takeIf { it != existing.sku.value }
            ?.let { requireUniqueSku(it) }

        existing.applyPatch(dto)
        return repo.save(existing).toDto()
    }

    override fun delete(id: UUID) {
        val deleted = repo.delete(ProductId(id))
        if (!deleted)
            throw NoSuchElementException("Product $id not found")
    }

    private fun requireUniqueSku(sku: String) {
        repo.findBySku(Sku(sku))
            ?: throw IllegalArgumentException("SKU '$sku' already exists")
    }

}
