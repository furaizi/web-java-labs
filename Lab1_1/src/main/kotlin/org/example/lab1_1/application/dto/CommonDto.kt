package org.example.lab1_1.application.dto

import java.math.BigDecimal

data class MoneyDto(
    val amount: BigDecimal,
    val currency: String
)