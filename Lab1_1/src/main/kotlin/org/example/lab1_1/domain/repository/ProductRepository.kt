package org.example.lab1_1.domain.repository

import org.example.lab1_1.domain.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    fun findBySku(sku: String): Product?
    fun findByPublicId(publicId: UUID): Product?
}
