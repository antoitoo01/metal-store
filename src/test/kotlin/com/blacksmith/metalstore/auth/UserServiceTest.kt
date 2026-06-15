package com.blacksmith.metalstore.auth

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.UpdateUserRequest
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.auth.service.UserService
import com.blacksmith.metalstore.organization.domain.entity.Organization
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var organizationRepository: OrganizationRepository

    private lateinit var supabaseAuthClient: SupabaseAuthClient
    private lateinit var userService: UserService

    private val organizationId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()
    private val otherId = UUID.randomUUID()
    private val adminId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        organizationRepository.deleteAll()
        supabaseAuthClient = mock<SupabaseAuthClient>()
        userService = UserService(userRepository, supabaseAuthClient)

        organizationRepository.save(Organization(id = organizationId, name = "Test Taller", slug = "test-taller"))

        userRepository.save(User(id = ownerId, tenantId = organizationId, username = "owner", email = "owner@test.com", role = Role.USER))
        userRepository.save(User(id = otherId, tenantId = organizationId, username = "other", email = "other@test.com", role = Role.USER))
        userRepository.save(User(id = adminId, tenantId = organizationId, username = "admin", email = "admin@test.com", role = Role.ADMIN))
    }

    @Test
    fun `update own profile works`() {
        val request = UpdateUserRequest(username = "owner-updated", email = "owner@test.com")

        val result = userService.update(request, ownerId)

        assert(result.username == "owner-updated")
    }

    @Test
    fun `update email triggers supabase sync`() {
        val request = UpdateUserRequest(username = "owner", email = "newemail@test.com")

        userService.update(request, ownerId)

        verify(supabaseAuthClient).updateUserEmail(ownerId, "newemail@test.com")
    }

    @Test
    fun `update with nonexistent user throws exception`() {
        val request = UpdateUserRequest(username = "ghost", email = "ghost@test.com")
        val fakeId = UUID.randomUUID()

        assertThrows<com.blacksmith.metalstore.auth.exception.UserNotFoundException> {
            userService.update(request, fakeId)
        }
    }

    @Test
    fun `update enforces username uniqueness`() {
        val request = UpdateUserRequest(username = "other", email = "owner@test.com")

        assertThrows<com.blacksmith.metalstore.auth.exception.UserAlreadyExistsException> {
            userService.update(request, ownerId)
        }
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
