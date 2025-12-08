package org.example.lab1_1.application.feature

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "feature")
class FeatureToggleProperties {
    val flags: MutableMap<String, FeatureToggleFlag> = mutableMapOf()
}

data class FeatureToggleFlag(
    var enabled: Boolean = false
)
