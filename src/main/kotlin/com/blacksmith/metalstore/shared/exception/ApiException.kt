package com.blacksmith.metalstore.shared.exception

open class ApiException(
    open val errorCode: ErrorCode,
    override val message: String
) : RuntimeException(message)
