package com.blacksmith.metalstore.auth.exception

sealed class AuthException(message: String) : RuntimeException(message)

class UserNotFoundException(message: String = "User not found") :
    AuthException(message)

class UserAlreadyExistsException(message: String = "User already exists") :
    AuthException(message)
