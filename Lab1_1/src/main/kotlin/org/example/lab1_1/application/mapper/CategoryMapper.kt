package org.example.lab1_1.application.mapper

import org.example.lab1_1.application.dto.CategoryCreateDto
import org.example.lab1_1.application.dto.CategoryDto
import org.example.lab1_1.domain.product.Category
import org.example.lab1_1.domain.product.CategoryId
import java.util.UUID

fun Category.toDto() = CategoryDto(
    id = id.value,
    name = name,
    parentId = parentId?.value,
    createdAt = createdAt,
    updatedAt = updatedAt
)

object CategoryFactory {
    fun fromCreate(
        dto: CategoryCreateDto,
        id: UUID = UUID.randomUUID()
    ) = Category(
        id = CategoryId(id),
        name = dto.name,
        parentId = dto.parentId?.let(::CategoryId)
    )
}