package org.example.lab1_1.application.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldContainExactly
import org.example.lab1_1.application.feature.FeatureNotAvailableException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "feature.cosmo-cats.enabled=true",
        "feature.kitty-products.enabled=true"
    ]
)
class CosmoCatServiceFeatureEnabledTest {

    @Autowired
    lateinit var service: CosmoCatService

    @Test
    fun `returns cosmo cats when feature is enabled`() {
        service.getCosmoCats() shouldContainExactly listOf("Luna", "Nova", "Orion")
    }
}

@SpringBootTest(
    properties = [
        "feature.cosmo-cats.enabled=false",
        "feature.kitty-products.enabled=true"
    ]
)
class CosmoCatServiceFeatureDisabledTest {

    @Autowired
    lateinit var service: CosmoCatService

    @Test
    fun `throws when cosmo cats feature is disabled`() {
        shouldThrowExactly<FeatureNotAvailableException> {
            service.getCosmoCats()
        }
    }
}
