package org.example.lab1_1.application.service

import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductDetailsDto
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val orderItemRepository: OrderItemRepository,
) : ProductService {

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
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), buildSort(sort))
        val spec = buildSpecification(q, categoryId, status, minPrice, maxPrice)
        val pageResult = productRepository.findAll(spec, pageable)
        return pageResult.toPageResult { toDto(it) }
    }

    override fun create(dto: ProductCreateDto): ProductDetailsDto {
        val category = resolveCategory(dto.categoryId)
        val product = Product(
            category = category,
            sku = dto.sku,
            name = dto.name,
            description = dto.description,
            priceAmount = dto.price,
            currencyCode = dto.currency,
            stockQuantity = 0,
            status = dto.status.toModel()
        )
        val saved = productRepository.save(product)
        return toDto(saved)
    }

    @Transactional(readOnly = true)
    override fun get(id: UUID): ProductDetailsDto =
        productRepository.findByPublicId(id)?.let { toDto(it) }
            ?: throw NoSuchElementException("Product $id not found")

    override fun replace(id: UUID, dto: ProductCreateDto): ProductDetailsDto {
        val product = productRepository.findByPublicId(id) ?: throw NoSuchElementException("Product $id not found")
        product.category = resolveCategory(dto.categoryId)
        product.sku = dto.sku
        product.name = dto.name
        product.description = dto.description
        product.priceAmount = dto.price
        product.currencyCode = dto.currency
        product.status = dto.status.toModel()
        val saved = productRepository.save(product)
        return toDto(saved)
    }

    override fun patch(id: UUID, dto: ProductPatchDto): ProductDetailsDto {
        val product = productRepository.findByPublicId(id) ?: throw NoSuchElementException("Product $id not found")
        dto.categoryId?.let { product.category = resolveCategory(it) }
        dto.sku?.let { product.sku = it }
        dto.name?.let { product.name = it }
        dto.description?.let { product.description = it }
        dto.price?.let { product.priceAmount = it }
        dto.currency?.let { product.currencyCode = it }
        dto.status?.let { product.status = it.toModel() }
        val saved = productRepository.save(product)
        return toDto(saved)
    }

    override fun delete(id: UUID) {
        val product = productRepository.findByPublicId(id) ?: throw NoSuchElementException("Product $id not found")
        productRepository.delete(product)
    }

    @Transactional(readOnly = true)
    override fun findTopSelling(limit: Int): List<ProductSalesProjection> {
        val pageSize = if (limit > 0) limit else 10
        return orderItemRepository.findTopProducts(PageRequest.of(0, pageSize))
    }

    private fun buildSpecification(
        q: String?,
        categoryId: UUID?,
        status: ProductStatusDto?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?
    ): Specification<Product> {
        var spec: Specification<Product> = Specification.where(null)
        if (!q.isNullOrBlank()) {
            val term = "%${q.trim().lowercase()}%"
            spec = spec.and { root, _, cb ->
                cb.or(
                    cb.like(cb.lower(root.get("name")), term),
                    cb.like(cb.lower(root.get("sku")), term)
                )
            }
        }
        if (categoryId != null) {
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Category>("category").get<UUID>("publicId"), categoryId)
            }
        }
        if (status != null) {
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<ProductStatus>("status"), status.toModel())
            }
        }
        if (minPrice != null) {
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("priceAmount"), minPrice)
            }
        }
        if (maxPrice != null) {
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("priceAmount"), maxPrice)
            }
        }
        return spec
    }

    private fun buildSort(sort: List<String>): Sort {
        if (sort.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt")
        }
        val orders = sort.mapNotNull { raw ->
            val parts = raw.split(",")
            val property = when (parts.first().trim()) {
                "name" -> "name"
                "sku" -> "sku"
                "price", "priceAmount" -> "priceAmount"
                "createdAt" -> "createdAt"
                "updatedAt" -> "updatedAt"
                "status" -> "status"
                else -> null
            } ?: return@mapNotNull null
            val direction = parts.getOrNull(1)?.let {
                if (it.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC
            } ?: Sort.Direction.ASC
            Sort.Order(direction, property)
        }
        return if (orders.isNotEmpty()) Sort.by(orders) else Sort.by(Sort.Direction.DESC, "createdAt")
    }

    private fun resolveCategory(id: UUID?): Category {
        requireNotNull(id) { "Category id is required" }
        return categoryRepository.findByPublicId(id) ?: throw NoSuchElementException("Category $id not found")
    }

    private fun toDto(product: Product): ProductDetailsDto = ProductDetailsDto(
        id = product.publicId,
        sku = product.sku,
        name = product.name,
        description = product.description,
        price = product.priceAmount,
        currency = product.currencyCode,
        categoryId = product.category?.publicId,
        status = product.status.toDto(),
        createdAt = product.createdAt?.toInstant() ?: Instant.EPOCH,
        updatedAt = product.updatedAt?.toInstant() ?: Instant.EPOCH
    )

    private fun ProductStatusDto.toModel(): ProductStatus = when (this) {
        ProductStatusDto.DRAFT -> ProductStatus.DRAFT
        ProductStatusDto.ACTIVE -> ProductStatus.ACTIVE
        ProductStatusDto.ARCHIVED -> ProductStatus.ARCHIVED
    }

    private fun ProductStatus.toDto(): ProductStatusDto = when (this) {
        ProductStatus.DRAFT -> ProductStatusDto.DRAFT
        ProductStatus.ACTIVE -> ProductStatusDto.ACTIVE
        ProductStatus.ARCHIVED -> ProductStatusDto.ARCHIVED
    }

    private fun <T, R> Page<T>.toPageResult(mapper: (T) -> R): PageResult<R> =
        PageResult(
            content = content.map(mapper),
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
}
