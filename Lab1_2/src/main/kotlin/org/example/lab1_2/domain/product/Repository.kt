package org.example.lab1_2.domain.product

import org.example.lab1_2.domain.common.PageRequest
import org.example.lab1_2.domain.common.PageResult
import org.example.lab1_2.domain.common.Sort
import java.util.UUID

interface ProductRepository {
    fun nextId(): ProductId = ProductId(UUID.randomUUID())

    fun findById(id: ProductId): Product?
    fun save(aggregate: Product): Product
    fun delete(id: ProductId): Boolean
    fun findBySku(sku: Sku): Product?

    fun search(
        filter: ProductSearch = ProductSearch(),
        page: PageRequest = PageRequest(),
        sort: List<Sort> = emptyList()
    ): PageResult<Product>
}

interface CategoryRepository {
    fun nextId(): CategoryId =
        CategoryId(UUID.randomUUID())

    fun findById(id: CategoryId): Category?
    fun save(aggregate: Category): Category
    fun delete(id: CategoryId)
}