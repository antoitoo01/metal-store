package com.blacksmith.metalstore.catalog.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.storage")
data class StorageProperties(
    var bucketName: String = "catalog-images",
    var uploadDir: String = "uploads"
)
