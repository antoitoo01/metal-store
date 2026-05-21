package com.blacksmith.metalstore.auth.repository

import com.blacksmith.metalstore.auth.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>

    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}