package com.blacksmith.metalstore.auth

import com.blacksmith.metalstore.auth.config.RateLimitingFilter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "rate-limiting.default-limit=5",
    "rate-limiting.default-window-seconds=60",
    "rate-limiting.excluded-paths="
])
class RateLimitingFilterTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var rateLimitingFilter: RateLimitingFilter

    private val organizationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        rateLimitingFilter.clearBuckets()
    }

    @Test
    fun `first request succeeds`() {
        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", "192.168.1.1"))
            .andExpect(status().isOk)
    }

    @Test
    fun `returns 429 after exceeding limit from same IP`() {
        val ip = "10.0.0.1"

        repeat(5) {
            mockMvc.perform(get("/api/inventory")
                .header("X-Organization-Id", organizationId.toString())
                .header("X-Forwarded-For", ip))
                .andExpect(status().isOk)
        }

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", ip))
            .andExpect(status().`is`(429))
            .andExpect(jsonPath("$.title").value("Too Many Requests"))
            .andExpect(jsonPath("$.status").value(429))
    }

    @Test
    fun `different IPs have independent rate limits`() {
        repeat(5) {
            mockMvc.perform(get("/api/inventory")
                .header("X-Organization-Id", organizationId.toString())
                .header("X-Forwarded-For", "192.168.1.10"))
                .andExpect(status().isOk)
        }

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", "192.168.1.10"))
            .andExpect(status().`is`(429))

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", "10.0.0.99"))
            .andExpect(status().isOk)
    }

    @Test
    fun `clearBuckets resets rate limit`() {
        val ip = "172.16.0.1"

        repeat(5) {
            mockMvc.perform(get("/api/inventory")
                .header("X-Organization-Id", organizationId.toString())
                .header("X-Forwarded-For", ip))
                .andExpect(status().isOk)
        }

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", ip))
            .andExpect(status().`is`(429))

        rateLimitingFilter.clearBuckets()

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", ip))
            .andExpect(status().isOk)
    }

    @Test
    fun `Retry-After header present on 429`() {
        val ip = "203.0.113.1"

        repeat(5) {
            mockMvc.perform(get("/api/inventory")
                .header("X-Organization-Id", organizationId.toString())
                .header("X-Forwarded-For", ip))
        }

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .header("X-Forwarded-For", ip))
            .andExpect(status().`is`(429))
            .andExpect(MockMvcResultMatchers.header().exists("Retry-After"))
    }
}