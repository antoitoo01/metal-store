package com.blacksmith.metalstore.catalog.integration

import com.blacksmith.metalstore.auth.config.SupabaseProperties
import com.blacksmith.metalstore.catalog.infrastructure.storage.SupabaseFileStorageService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupabaseStorageIntegrationTest {

    @Autowired
    private lateinit var props: SupabaseProperties

    private lateinit var storage: SupabaseFileStorageService
    private val namespace = "inttest"
    private val filename = "test-${UUID.randomUUID().toString().take(8)}.png"
    private var imageUrl: String? = null

    private val testImageBytes = byteArrayOf(
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
        0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0, -106, 77, -37, 106,
        0, 0, 0, 12, 73, 68, 65, 84, 8, -39, 99, 96, 0, 0, 0, 2,
        0, 1, -27, -63, -62, 48, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    )

    @BeforeAll
    fun setup() {
        assumeTrue(props.url.isNotBlank()) { "SUPABASE_URL not set — skipping real storage integration tests" }
        assumeTrue(props.secretKey.isNotBlank()) { "SUPABASE_SECRET_KEY not set — skipping" }
        storage = SupabaseFileStorageService(props, "catalog-images")
    }

    @Test
    fun `upload saves file to Supabase Storage and returns public URL`() {
        val url = storage.save(namespace, filename, testImageBytes, "image/png")
        imageUrl = url

        assertTrue(url.startsWith(props.url), "URL should point to Supabase Storage")
        assertTrue(url.contains(namespace), "URL should contain namespace")
        assertTrue(url.contains(filename), "URL should contain filename")
    }

    @Test
    fun `load retrieves file bytes matching what was uploaded`() {
        assertNotNull(imageUrl) { "Run upload test first" }

        val loaded = storage.load(imageUrl!!)
        assertNotNull(loaded, "Should retrieve file bytes")
        assertArrayEquals(testImageBytes, loaded, "Bytes should match uploaded content")
    }

    @Test
    fun `delete removes file from storage`() {
        assertNotNull(imageUrl) { "Run upload test first" }

        assertDoesNotThrow { storage.delete(imageUrl!!) }

        val afterDelete = storage.load(imageUrl!!)
        assertNull(afterDelete, "File should no longer exist after delete")
    }
}
