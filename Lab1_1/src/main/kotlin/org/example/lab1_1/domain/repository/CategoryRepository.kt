package org.example.lab1_1.domain.repository

import org.example.lab1_1.domain.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByPublicId(publicId: UUID): Category?
}
