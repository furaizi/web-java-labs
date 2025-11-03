package org.example.lab1_1.application.validation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.example.lab1_1.application.validation.annotation.CosmicWordCheck

class CosmicWordValidator : ConstraintValidator<CosmicWordCheck, String?> {

    private var allowEmpty: Boolean = false
    private lateinit var words: List<String>

    override fun initialize(annotation: CosmicWordCheck) {
        allowEmpty = annotation.allowEmpty
        words = annotation.words
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) {
            return allowEmpty
        }

        val hay = value.lowercase()
        return words.any { it in hay }
    }
}