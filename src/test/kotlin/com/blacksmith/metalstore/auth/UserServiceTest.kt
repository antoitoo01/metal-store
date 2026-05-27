package com.blacksmith.metalstore.auth

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.UpdateUserRequest
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.Tenant
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.repository.TenantRepository
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.auth.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tenantRepository: TenantRepository

    private lateinit var supabaseAuthClient: SupabaseAuthClient
    private lateinit var userService: UserService

    private val tenantId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()
    private val otherId = UUID.randomUUID()
    private val adminId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        tenantRepository.deleteAll()
        supabaseAuthClient = mock<SupabaseAuthClient>()
        userService = UserService(userRepository, supabaseAuthClient)

        val tenant = Tenant(id = tenantId, name = "Test Taller", slug = "test-taller")
        tenantRepository.save(tenant)

        userRepository.save(User(id = ownerId, tenantId = tenantId, username = "owner", email = "owner@test.com", role = Role.USER, createdDate = LocalDateTime.now(), lastModifiedDate = LocalDateTime.now()))
        userRepository.save(User(id = otherId, tenantId = tenantId, username = "other", email = "other@test.com", role = Role.USER, createdDate = LocalDateTime.now(), lastModifiedDate = LocalDateTime.now()))
        userRepository.save(User(id = adminId, tenantId = tenantId, username = "admin", email = "admin@test.com", role = Role.ADMIN, createdDate = LocalDateTime.now(), lastModifiedDate = LocalDateTime.now()))
    }

    @Test
    fun `update own profile works`() {
        val request = UpdateUserRequest(id = ownerId, username = "owner-updated", email = "owner@test.com")

        val result = userService.update(request, ownerId)

        assert(result.username == "owner-updated")
    }

    @Test
    fun `update email triggers supabase sync`() {
        val request = UpdateUserRequest(id = ownerId, username = "owner", email = "newemail@test.com")

        userService.update(request, ownerId)

        verify(supabaseAuthClient).updateUserEmail(ownerId, "newemail@test.com")
    }

    @Test
    fun `non-admin cannot update another user`() {
        val request = UpdateUserRequest(id = otherId, username = "hacked", email = "other@test.com")

        assertThrows<AccessDeniedException> {
            userService.update(request, ownerId)
        }
    }

    @Test
    fun `admin can update another user`() {
        val request = UpdateUserRequest(id = otherId, username = "admin-updated", email = "other@test.com")

        val result = userService.update(request, adminId)

        assert(result.username == "admin-updated")
    }

    @Test
    fun `delete own profile works`() {
        userService.deleteById(ownerId, ownerId)

        assert(userRepository.findById(ownerId).isEmpty)
        verify(supabaseAuthClient).deleteUser(ownerId)
    }

    @Test
    fun `non-admin cannot delete another user`() {
        assertThrows<AccessDeniedException> {
            userService.deleteById(otherId, ownerId)
        }
    }

    @Test
    fun `admin can delete another user`() {
        userService.deleteById(otherId, adminId)

        assert(userRepository.findById(otherId).isEmpty)
        verify(supabaseAuthClient).deleteUser(otherId)
    }

    @Test
    fun `delete user by entity works`() {
        val user = userRepository.findById(ownerId).get()

        userService.delete(user, ownerId)

        assert(userRepository.findById(ownerId).isEmpty)
        verify(supabaseAuthClient).deleteUser(ownerId)
    }
}
