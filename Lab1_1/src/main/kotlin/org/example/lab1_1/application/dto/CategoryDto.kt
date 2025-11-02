package org.example.lab1_1.application.dto

import org.example.lab1_1.application.validation.annotation.CosmicName
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
    @field:CosmicName
    val name: String,
    val parentId: UUID? = null
)