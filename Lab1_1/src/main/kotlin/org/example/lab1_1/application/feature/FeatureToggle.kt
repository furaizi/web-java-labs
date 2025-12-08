package org.example.lab1_1.application.feature

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureToggle(
    val value: String
)
