package org.example.lab1_1.application.mapper

import org.example.lab1_1.application.dto.ProductCreateDto
import org.example.lab1_1.application.dto.ProductDetailsDto
import org.example.lab1_1.application.dto.ProductPatchDto
import org.example.lab1_1.domain.product.CategoryId
import org.example.lab1_1.domain.product.Product
import org.example.lab1_1.domain.product.ProductId
import org.example.lab1_1.domain.product.Sku
import java.util.UUID

fun Product.toDto() = ProductDetailsDto(
    id = id.value,
    sku = sku.value,
    name = name,
    description = description,
    price = price.amount,
    currency = currency.code(),
    categoryId = categoryId?.value,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

object ProductFactory {
    fun fromCreate(
        dto: ProductCreateDto,
        id: UUID = UUID.randomUUID()
    ) = Product(
        id = ProductId(id),
        sku = Sku(dto.sku),
        name = dto.name,
        price = dto.price.asMoney(dto.currency.toCurrency()),
        currency = dto.currency.toCurrency(),
        categoryId = dto.categoryId?.let(::CategoryId),
        description = dto.description,
        status = dto.status
    )
}

fun Product.applyPatch(dto: ProductPatchDto) {
    dto.sku
        ?.let { Sku(it) }
        ?.also { this.changeSku(it) }

    dto.name
        ?.takeIf { it.isNotBlank() }
        ?.trim()
        ?.also { this.rename(it) }

    dto.description?.let {
        this.changeDescription(it)
    }

    dto.price
        ?.asMoney(currency)
        ?.also { this.changePrice(it) }

    dto.categoryId
        ?.let { CategoryId(it) }
        ?.also { this.relinkCategory(it) }

    dto.status?.let {
        when (it) {
            Product.Status.DRAFT -> { }
            Product.Status.ACTIVE   -> this.activate()
            Product.Status.ARCHIVED -> this.archive()
        }
    }
}