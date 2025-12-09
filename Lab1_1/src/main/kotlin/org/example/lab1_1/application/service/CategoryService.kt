package org.example.lab1_1.application.service

import org.example.lab1_1.domain.model.Category
import java.util.UUID

interface CategoryService {
    fun create(category: Category): Category
    fun get(id: UUID): Category
    fun getAll(): List<Category>
    fun update(id: UUID, update: Category): Category
    fun delete(id: UUID)
}
