package org.example.lab1_1.domain.common

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}