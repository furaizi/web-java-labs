package org.example.lab1_1.application.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.Size
import org.example.lab1_1.application.validation.validator.CosmicWordValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [CosmicWordValidator::class])
@ReportAsSingleViolation
@Size(min = 1, max = 120)
annotation class CosmicName(
    val message: String = "{cosmic.word.invalid}",
    val words: Array<String> = ["star", "galaxy", "nebula", "comet", "astro", "cosmic", "orbit"],
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)