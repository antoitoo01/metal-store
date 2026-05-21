package com.blacksmith.metalstore.auth.exception

sealed class AuthException(message: String) : RuntimeException(message)

class UserNotFoundException(userId: String) :
    AuthException("User with id $userId not found")

class UserAlreadyExistsException(username: String) :
    AuthException("User $username already exists")

