package org.example.lab1_1.application.feature

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties
class FeatureToggleProperties(
    val feature: MutableMap<String, Flag> = mutableMapOf()
) {

    data class Flag(
        val enabled: Boolean = false
    )

}
