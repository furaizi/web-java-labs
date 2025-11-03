package org.example.lab1_2.domain.product

import org.example.lab1_2.domain.common.AggregateRoot
import org.example.lab1_2.domain.common.Money
import java.time.Instant
import java.util.*


@JvmInline
value class ProductId(val value: UUID)

@JvmInline
value class Sku(val value: String) {
    init {
        require(value.matches(Regex("^[A-Z0-9-]{3,32}$"))) {
            "invalid sku"
        }
    }
}

class Product(
    val id: ProductId,
    sku: Sku,
    name: String,
    price: Money,
    val currency: Currency,
    categoryId: CategoryId?,
    description: String? = null,
    status: Status = Status.DRAFT,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = createdAt
) : AggregateRoot {

    enum class Status {
        DRAFT, ACTIVE, ARCHIVED
    }

    var sku = sku
        private set
    var name = name
        private set
    var description = description
        private set
    var price = price.normalized().nonNegative()
        private set
    var status = status
        private set
    var categoryId = categoryId
        private set

    fun activate() {
        require(status != Status.ARCHIVED) {
            "archived cannot be activated"
        }

        status = Status.ACTIVE
        touch()
    }

    fun archive() {
        status = Status.ARCHIVED
        touch()
    }

    fun rename(newName: String) {
        require(newName.isNotBlank())

        name = newName.trim()
        touch()
    }

    fun changePrice(newPrice: Money) {
        price = newPrice.normalized()
            .nonNegative()
        touch()
    }

    fun changeDescription(desc: String?){
        description = desc?.takeIf {
            it.isNotBlank()
        }
        touch()
    }

    fun relinkCategory(newCategoryId: CategoryId?) {
        categoryId = newCategoryId
        touch()
    }

    fun changeSku(newSku: Sku) {
        sku = newSku
        touch()
    }

    private fun touch() {
        updatedAt = Instant.now()
    }
}