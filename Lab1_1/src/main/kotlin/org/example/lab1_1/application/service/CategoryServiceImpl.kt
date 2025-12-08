package org.example.lab1_1.application.service

import org.example.lab1_1.domain.model.Category
import org.example.lab1_1.domain.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
) : CategoryService {

    override fun create(category: Category): Category = categoryRepository.save(category)

    @Transactional(readOnly = true)
    override fun get(id: UUID): Category = findCategory(id)

    @Transactional(readOnly = true)
    override fun getAll(): List<Category> = categoryRepository.findAll()

    override fun update(id: UUID, update: Category): Category {
        val category = findCategory(id)
        category.name = update.name
        category.description = update.description
        return category
    }

    override fun delete(id: UUID) {
        val category = findCategory(id)
        categoryRepository.delete(category)
    }

    private fun findCategory(publicId: UUID): Category =
        categoryRepository.findByPublicId(publicId)
            ?: throw NoSuchElementException("Category $publicId not found")
}
