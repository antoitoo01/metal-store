package com.blacksmith.metalstore

import com.blacksmith.metalstore.config.loadDotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MetalStoreApplication

fun main(args: Array<String>) {
    loadDotenv()
    runApplication<MetalStoreApplication>(*args)
}
