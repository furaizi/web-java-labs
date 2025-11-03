package org.example.lab1_1.application.mapper

import org.example.lab1_1.domain.common.Money
import java.math.BigDecimal
import java.util.Currency

fun String.toCurrency(): Currency =
    Currency.getInstance(this)

fun Currency.code(): String =
    this.currencyCode

fun BigDecimal.asMoney(cur: Currency): Money =
    Money(this, cur)