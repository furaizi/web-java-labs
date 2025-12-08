package org.example.lab1_1.application.feature

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class FeatureToggleAspect(
    private val featureToggleService: FeatureToggleService
) {

    @Around("@annotation(featureToggle)")
    fun guardWithFeatureToggle(joinPoint: ProceedingJoinPoint, featureToggle: FeatureToggle): Any? {
        val featureName = featureToggle.value
        if (featureToggleService.isFeatureEnabled(featureName)) {
            return joinPoint.proceed()
        }
        throw FeatureNotAvailableException(featureName)
    }
}
