package org.example.lab1_1.application.service

import org.example.lab1_1.application.feature.FeatureToggle
import org.springframework.stereotype.Service

@Service
class CosmoCatService {

    @FeatureToggle("cosmo-cats")
    fun getCosmoCats(): List<String> = listOf(
        "Luna",
        "Nova",
        "Orion"
    )
}
