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
