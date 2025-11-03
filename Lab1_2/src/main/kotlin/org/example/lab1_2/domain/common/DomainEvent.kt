package org.example.lab1_2.domain.common

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}