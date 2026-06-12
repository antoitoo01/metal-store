package com.blacksmith.metalstore.auth.exception

import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.shared.exception.ErrorCode
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.ResourceAccessException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleApiException returns ProblemDetail with errorCode`() {
        val ex = ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid login")
        val problem = handler.handleApiException(ex)

        assert(problem.status == HttpStatus.UNAUTHORIZED.value())
        assert(problem.detail == "Invalid login")
        assert(problem.properties?.get("code") == "INVALID_CREDENTIALS")
    }

    @Test
    fun `handleApiException maps RESOURCE_NOT_FOUND to 404`() {
        val ex = ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Not found")
        val problem = handler.handleApiException(ex)

        assert(problem.status == HttpStatus.NOT_FOUND.value())
        assert(problem.properties?.get("code") == "RESOURCE_NOT_FOUND")
    }

    @Test
    fun `handleApiException maps INTERNAL_ERROR to 500`() {
        val ex = ApiException(ErrorCode.INTERNAL_ERROR, "Unexpected error")
        val problem = handler.handleApiException(ex)

        assert(problem.status == HttpStatus.INTERNAL_SERVER_ERROR.value())
        assert(problem.properties?.get("code") == "INTERNAL_ERROR")
    }

    @Test
    fun `handleResourceAccess returns 503 SERVICE_UNAVAILABLE`() {
        val ex = ResourceAccessException("Connection refused")
        val problem = handler.handleResourceAccess(ex)

        assert(problem.status == HttpStatus.SERVICE_UNAVAILABLE.value())
        assert(problem.detail == "External service is temporarily unavailable")
        assert(problem.properties?.get("code") == "SERVICE_UNAVAILABLE")
    }
}