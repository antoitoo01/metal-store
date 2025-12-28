package com.blacksmith.metalstore.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MetalStoreAuthServiceApplication

fun main(args: Array<String>) {
	runApplication<MetalStoreAuthServiceApplication>(*args)
}
