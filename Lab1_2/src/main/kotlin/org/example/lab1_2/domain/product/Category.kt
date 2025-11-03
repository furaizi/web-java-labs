package org.example.lab1_2.domain.product

import org.example.lab1_2.domain.common.AggregateRoot
import java.time.Instant
import java.util.UUID

@JvmInline
value class CategoryId(val value: UUID)

class Category(
    val id: CategoryId,
    name: String,
    val parentId: CategoryId? = null,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = createdAt
) : AggregateRoot {

    var name: String = name
        private set

    fun rename(newName: String) {
        require(newName.isNotBlank())

        name = newName.trim()
        updatedAt = Instant.now()
    }
}