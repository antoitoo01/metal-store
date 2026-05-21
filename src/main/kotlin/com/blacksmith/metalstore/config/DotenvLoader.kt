package com.blacksmith.metalstore.config

import java.io.File

fun loadDotenv() {
    val file = File(".env")
    if (!file.exists()) return

    file.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
        val eq = trimmed.indexOf('=')
        if (eq <= 0) return@forEach
        val key = trimmed.substring(0, eq)
        val value = trimmed.substring(eq + 1)
        if (System.getProperty(key) == null) {
            System.setProperty(key, value)
        }
    }
}
