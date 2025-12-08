package org.example.lab1_1.domain.repository

import org.example.lab1_1.domain.model.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, Long> {
    fun findByEmail(email: String): Customer?
    fun findByPublicId(publicId: UUID): Customer?
}
