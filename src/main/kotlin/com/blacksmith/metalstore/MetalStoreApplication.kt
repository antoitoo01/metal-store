package com.blacksmith.metalstore

import com.blacksmith.metalstore.config.loadDotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.web.config.EnableSpringDataWebSupport

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class MetalStoreApplication

fun main(args: Array<String>) {
    loadDotenv()
    runApplication<MetalStoreApplication>(*args)
}
