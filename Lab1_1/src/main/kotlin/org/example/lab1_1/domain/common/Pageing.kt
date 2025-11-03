package org.example.lab1_1.domain.common

data class PageRequest(val page: Int = 0, val size: Int = 20) {
    init {
        require(page >= 0 && size > 0)
    }
}

enum class Direction {
    ASC, DESC
}

data class Sort(
    val field: String,
    val direction: Direction = Direction.ASC
)

data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)