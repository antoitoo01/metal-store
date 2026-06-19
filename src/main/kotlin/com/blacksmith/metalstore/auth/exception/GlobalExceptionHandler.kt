package com.blacksmith.metalstore.shared.exception

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.dao.DataIntegrityViolationException
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

    private fun mdcContext(): String = buildString {
        val traceId = MDC.get("traceId")
        if (traceId != null) append("traceId=$traceId ")
        val path = MDC.get("path")
        if (path != null) append("path=$path ")
        val method = MDC.get("method")
        if (method != null) append("method=$method ")
        val clientIp = MDC.get("clientIp")
        if (clientIp != null) append("clientIp=$clientIp")
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ProblemDetail {
        log.warn("API error [{}]: {} [{}]", ex.errorCode.name, ex.message, mdcContext())
        val problem = ProblemDetail.forStatusAndDetail(ex.errorCode.httpStatus, ex.message)
        problem.setProperty("code", ex.errorCode.name)
        return problem
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ProblemDetail {
        log.warn("Data integrity violation: {} [{}]", ex.message, mdcContext())
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Data integrity violation")
    }

    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccess(ex: ResourceAccessException): ProblemDetail {
        log.error("External service unavailable: {} [{}]", ex.message, mdcContext())
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE, "External service is temporarily unavailable"
        )
        problem.setProperty("code", ErrorCode.SERVICE_UNAVAILABLE.name)
        return problem
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ProblemDetail {
        log.warn("Malformed request body: {} [{}]", ex.message, mdcContext())
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail {
        log.warn("Bad request: {} [{}]", ex.message, mdcContext())
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ProblemDetail {
        log.warn("Validation failed: {} [{}]", ex.message, mdcContext())
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed")
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        problem.setProperty("errors", errors)
        return problem
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDenied(ex: org.springframework.security.access.AccessDeniedException): ProblemDetail {
        log.warn("Access denied: {} [{}]", ex.message, mdcContext())
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.message ?: "Access Denied")
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ProblemDetail {
        log.warn("Media type not supported: {} [{}]", ex.contentType, mdcContext())
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type '${ex.contentType}' is not supported")
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ProblemDetail {
        log.warn("Method not supported: {} {} [{}]", ex.method, ex.message, mdcContext())
        return ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed")
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ProblemDetail {
        log.error("Internal server error [traceId={}, path={}, method={}, clientIp={}]",
            MDC.get("traceId"), MDC.get("path"), MDC.get("method"), MDC.get("clientIp"), ex)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        )
    }
}
