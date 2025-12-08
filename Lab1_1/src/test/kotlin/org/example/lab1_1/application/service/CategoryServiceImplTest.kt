package org.example.lab1_1.application.service

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.lab1_1.domain.model.Category
import org.example.lab1_1.domain.repository.CategoryRepository
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

class CategoryServiceImplTest {

    private val repo: CategoryRepository = mockk(relaxed = true)
    private val service = CategoryServiceImpl(repo)

    @Test
    fun `create delegates to repository`() {
        val category = Category(name = "Stars")
        every { repo.save(category) } returns category

        service.create(category) shouldBe category

        verify(exactly = 1) { repo.save(category) }
    }

    @Test
    fun `get resolves by public id`() {
        val publicId = UUID.randomUUID()
        val category = Category(publicId = publicId, name = "Nebulae")
        every { repo.findByPublicId(publicId) } returns category

        service.get(publicId) shouldBe category

        verify(exactly = 1) { repo.findByPublicId(publicId) }
    }

    @Test
    fun `update mutates entity fields`() {
        val publicId = UUID.randomUUID()
        val existing = Category(publicId = publicId, name = "Old", description = "old")
        val update = Category(name = "New", description = "new")
        every { repo.findByPublicId(publicId) } returns existing

        val result = service.update(publicId, update)

        result.name shouldBe "New"
        result.description shouldBe "new"
    }

    @Test
    fun `getAll delegates to repository`() {
        val categories = listOf(Category(name = "A"), Category(name = "B"))
        every { repo.findAll() } returns categories

        service.getAll() shouldContainExactly categories
    }

    @Test
    fun `delete removes resolved entity`() {
        val publicId = UUID.randomUUID()
        val category = Category(publicId = publicId, name = "ToRemove")
        every { repo.findByPublicId(publicId) } returns category

        service.delete(publicId)

        verify(exactly = 1) { repo.delete(category) }
    }
}
