package com.blacksmith.metalstore.shared.exception

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ErrorCodeTest {

    @Test
    fun `VALIDATION_ERROR has 400 BAD_REQUEST`() {
        assert(ErrorCode.VALIDATION_ERROR.httpStatus == HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `MALFORMED_REQUEST has 400 BAD_REQUEST`() {
        assert(ErrorCode.MALFORMED_REQUEST.httpStatus == HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `MISSING_TENANT has 400 BAD_REQUEST`() {
        assert(ErrorCode.MISSING_TENANT.httpStatus == HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `INVALID_TENANT has 400 BAD_REQUEST`() {
        assert(ErrorCode.INVALID_TENANT.httpStatus == HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `INVALID_CREDENTIALS has 401 UNAUTHORIZED`() {
        assert(ErrorCode.INVALID_CREDENTIALS.httpStatus == HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `TOKEN_EXPIRED has 401 UNAUTHORIZED`() {
        assert(ErrorCode.TOKEN_EXPIRED.httpStatus == HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `INVALID_TOKEN has 401 UNAUTHORIZED`() {
        assert(ErrorCode.INVALID_TOKEN.httpStatus == HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `UNAUTHORIZED has 401 UNAUTHORIZED`() {
        assert(ErrorCode.UNAUTHORIZED.httpStatus == HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `FORBIDDEN has 403 FORBIDDEN`() {
        assert(ErrorCode.FORBIDDEN.httpStatus == HttpStatus.FORBIDDEN)
    }

    @Test
    fun `RESOURCE_NOT_FOUND has 404 NOT_FOUND`() {
        assert(ErrorCode.RESOURCE_NOT_FOUND.httpStatus == HttpStatus.NOT_FOUND)
    }

    @Test
    fun `METHOD_NOT_ALLOWED has 405 METHOD_NOT_ALLOWED`() {
        assert(ErrorCode.METHOD_NOT_ALLOWED.httpStatus == HttpStatus.METHOD_NOT_ALLOWED)
    }

    @Test
    fun `RESOURCE_CONFLICT has 409 CONFLICT`() {
        assert(ErrorCode.RESOURCE_CONFLICT.httpStatus == HttpStatus.CONFLICT)
    }

    @Test
    fun `INVALID_STATE_TRANSITION has 409 CONFLICT`() {
        assert(ErrorCode.INVALID_STATE_TRANSITION.httpStatus == HttpStatus.CONFLICT)
    }

    @Test
    fun `MEDIA_TYPE_NOT_SUPPORTED has 415 UNSUPPORTED_MEDIA_TYPE`() {
        assert(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.httpStatus == HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    @Test
    fun `RATE_LIMIT_EXCEEDED has 429 TOO_MANY_REQUESTS`() {
        assert(ErrorCode.RATE_LIMIT_EXCEEDED.httpStatus == HttpStatus.TOO_MANY_REQUESTS)
    }

    @Test
    fun `INTERNAL_ERROR has 500 INTERNAL_SERVER_ERROR`() {
        assert(ErrorCode.INTERNAL_ERROR.httpStatus == HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `EXTERNAL_SERVICE_ERROR has 502 BAD_GATEWAY`() {
        assert(ErrorCode.EXTERNAL_SERVICE_ERROR.httpStatus == HttpStatus.BAD_GATEWAY)
    }

    @Test
    fun `SERVICE_UNAVAILABLE has 503 SERVICE_UNAVAILABLE`() {
        assert(ErrorCode.SERVICE_UNAVAILABLE.httpStatus == HttpStatus.SERVICE_UNAVAILABLE)
    }
}
