package org.example.lab1_1.application.dto

import java.time.Instant
import java.util.UUID

data class CategoryDto(
    val id: UUID,
    val name: String,
    val parentId: UUID? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class CategoryCreateDto(
    val name: String,
    val parentId: UUID? = null
)