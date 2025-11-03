package org.example.lab1_2.infrastructure.repository

import org.example.lab1_2.domain.common.Direction
import org.example.lab1_2.domain.common.PageRequest
import org.example.lab1_2.domain.common.PageResult
import org.example.lab1_2.domain.common.Sort
import org.example.lab1_2.domain.product.*
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

@Primary
@Repository
class InMemoryProductRepository : ProductRepository {

    private val store = ConcurrentHashMap<ProductId, Product>()

    override fun nextId() = ProductId(UUID.randomUUID())

    override fun findById(id: ProductId) = store[id]

    override fun save(aggregate: Product): Product {
        store[aggregate.id] = aggregate
        return aggregate
    }

    override fun delete(id: ProductId) =
        store.remove(id) != null

    override fun findBySku(sku: Sku) =
        store.values.firstOrNull { it.sku == sku }

    fun findAll(): List<Product> = store.values.toList()
    fun clear() = store.clear()

    override fun search(
        filter: ProductSearch,
        page: PageRequest,
        sort: List<Sort>
    ): PageResult<Product> {
        val filtered = store.values
            .asSequence()
            .let { seq ->
                filter.q?.takeIf { it.isNotBlank() }?.let {
                    val needle = it.trim().lowercase()
                    seq.filter { p ->
                        p.name.lowercase().contains(needle) ||
                                p.sku.value.lowercase().contains(needle) ||
                                (p.description?.lowercase()?.contains(needle) == true)
                    }
                } ?: seq
            }
            .let { seq ->
                filter.categoryId?.let { cid ->
                    seq.filter { it.categoryId?.value == cid }
                } ?: seq
            }
            .let { seq ->
                filter.status?.let { st ->
                    seq.filter { it.status == st }
                } ?: seq
            }
            .let { seq ->
                filter.minPrice?.let { min ->
                    seq.filter { it.price.amount >= min }
                } ?: seq
            }
            .let { seq ->
                filter.maxPrice?.let { maxP ->
                    seq.filter { it.price.amount <= maxP }
                } ?: seq
            }
            .toList()

        val sorted = sort.fold(filtered) { acc, s ->
            val cmp = when (s.field) {
                "name"      -> compareBy<Product> { it.name }
                "price"     -> compareBy { it.price.amount }
                "createdAt" -> compareBy { it.createdAt }
                "updatedAt" -> compareBy { it.updatedAt }
                "sku"       -> compareBy { it.sku.value }
                else        -> null
            } ?: return@fold acc
            if (s.direction == Direction.DESC)
                acc.sortedWith(cmp.reversed())
            else
                acc.sortedWith(cmp)
        }

        val size = max(page.size, 1)
        val from = (page.page * size).coerceAtMost(sorted.size)
        val to = (from + size).coerceAtMost(sorted.size)
        val slice = if (from < to)
                sorted.subList(from, to)
            else
                emptyList()
        val total = sorted.size
        val totalPages = if (total == 0)
                0
            else
                (total + size - 1) / size

        return PageResult(
            content = slice,
            page = page.page,
            size = size,
            totalElements = total.toLong(),
            totalPages = totalPages
        )
    }
}