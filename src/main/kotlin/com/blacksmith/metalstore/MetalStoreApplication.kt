package com.blacksmith.metalstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MetalStoreApplication

fun main(args: Array<String>) {
	runApplication<MetalStoreApplication>(*args)
}
