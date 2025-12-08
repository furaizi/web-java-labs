package org.example.lab1_1.application.feature

import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    private val featureToggleProperties: FeatureToggleProperties
) {

    fun isFeatureEnabled(featureName: String): Boolean =
        featureToggleProperties
            .feature[featureName]
            ?.enabled ?: false
}
