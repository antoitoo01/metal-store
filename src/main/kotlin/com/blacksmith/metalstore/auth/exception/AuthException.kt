package com.blacksmith.metalstore.auth.exception

sealed class AuthException(message: String) : RuntimeException(message)

class UserNotFoundException(message: String = "User not found") :
    AuthException(message)

class UserAlreadyExistsException(message: String = "User already exists") :
    AuthException(message)

class MissingTenantIdException(message: String = "X-Tenant-Id header is required and must be a valid UUID") :
    AuthException(message)

class InvalidTenantIdException(message: String = "X-Tenant-Id header must be a valid UUID") :
    AuthException(message)
