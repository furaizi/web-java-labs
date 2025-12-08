package org.example.lab1_1.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(name = "product_seq_gen", sequenceName = "product_seq", allocationSize = 1)
    var id: Long? = null,
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    var publicId: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null,
    @Column(nullable = false, length = 32, unique = true)
    var sku: String = "",
    @Column(nullable = false, length = 255)
    var name: String = "",
    @Column(columnDefinition = "text")
    var description: String? = null,
    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2)
    var priceAmount: BigDecimal = BigDecimal.ZERO,
    @Column(name = "currency_code", nullable = false, length = 3)
    var currencyCode: String = "",
    @Column(name = "stock_quantity", nullable = false)
    var stockQuantity: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: ProductStatus = ProductStatus.DRAFT,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
) {
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnore
    val orderItems: MutableList<OrderItem> = mutableListOf()
}
