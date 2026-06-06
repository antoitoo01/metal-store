package com.blacksmith.metalstore.auth.exception

import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.shared.exception.ErrorCode

class UserNotFoundException(
    message: String = "User not found",
    override val errorCode: ErrorCode = ErrorCode.RESOURCE_NOT_FOUND
) : ApiException(errorCode, message)

class UserAlreadyExistsException(
    message: String = "User already exists",
    override val errorCode: ErrorCode = ErrorCode.RESOURCE_CONFLICT
) : ApiException(errorCode, message)

class MissingTenantIdException(
    message: String = "X-Tenant-Id header is required and must be a valid UUID",
    override val errorCode: ErrorCode = ErrorCode.MISSING_TENANT
) : ApiException(errorCode, message)

class InvalidTenantIdException(
    message: String = "X-Tenant-Id header must be a valid UUID",
    override val errorCode: ErrorCode = ErrorCode.INVALID_TENANT
) : ApiException(errorCode, message)
