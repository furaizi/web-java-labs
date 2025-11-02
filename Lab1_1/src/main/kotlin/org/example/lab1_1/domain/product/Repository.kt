package org.example.lab1_1.domain.product

import java.util.UUID

interface ProductRepository {
    fun nextId(): ProductId =
        ProductId(UUID.randomUUID())

    fun findById(id: ProductId): Product?
    fun save(aggregate: Product): Product
    fun delete(id: ProductId)
    fun findBySku(sku: Sku): Product?
}

interface CategoryRepository {
    fun nextId(): CategoryId =
        CategoryId(UUID.randomUUID())

    fun findById(id: CategoryId): Category?
    fun save(aggregate: Category): Category
    fun delete(id: CategoryId)
}