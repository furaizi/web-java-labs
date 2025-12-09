package org.example.lab1_1.domain.repository.projection

interface ProductSalesProjection {
    val productId: Long
    val productName: String
    val totalQuantity: Long
}
