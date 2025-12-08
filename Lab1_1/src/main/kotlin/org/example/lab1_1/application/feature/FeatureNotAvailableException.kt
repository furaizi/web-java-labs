package org.example.lab1_1.application.feature

class FeatureNotAvailableException(
    featureName: String
) : RuntimeException("Feature '$featureName' is not available")
