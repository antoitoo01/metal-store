package com.blacksmith.metalstore.catalog.infrastructure.storage

interface ImageStorageService {
    fun save(namespace: String, filename: String, bytes: ByteArray, contentType: String): String
    fun delete(imageUrl: String)
    fun load(imageUrl: String): ByteArray?
}
