package com.blacksmith.metalstore.auth.exception

import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.shared.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.ResourceAccessException

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ProblemDetail {
        log.warn("API error [{}]: {}", ex.errorCode.name, ex.message)
        val problem = ProblemDetail.forStatusAndDetail(ex.errorCode.httpStatus, ex.message)
        problem.setProperty("code", ex.errorCode.name)
        return problem
    }

    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccess(ex: ResourceAccessException): ProblemDetail {
        log.error("External service unavailable: {}", ex.message)
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE, "External service is temporarily unavailable"
        )
        problem.setProperty("code", ErrorCode.SERVICE_UNAVAILABLE.name)
        return problem
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ProblemDetail {
        log.warn("Malformed request body: {}", ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail {
        log.warn("Bad request: {}", ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ProblemDetail {
        log.warn("Validation failed: {}", ex.message)
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed")
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        problem.setProperty("errors", errors)
        return problem
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDenied(ex: org.springframework.security.access.AccessDeniedException): ProblemDetail {
        log.warn("Access denied: {}", ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.message ?: "Access Denied")
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ProblemDetail {
        log.warn("Media type not supported: {}", ex.contentType)
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type '${ex.contentType}' is not supported")
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ProblemDetail {
        log.warn("Method not supported: {} {}", ex.method, ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed")
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ProblemDetail {
        log.error("Internal server error", ex)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        )
    }
}
