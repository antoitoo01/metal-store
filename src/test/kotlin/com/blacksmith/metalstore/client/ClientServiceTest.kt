package com.blacksmith.metalstore.client

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.client.application.ClientService
import com.blacksmith.metalstore.client.domain.entity.Client
import com.blacksmith.metalstore.client.domain.entity.ClientStatus
import com.blacksmith.metalstore.client.domain.repository.ClientRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class ClientServiceTest {

    @Autowired
    private lateinit var repo: ClientRepository

    private lateinit var service: ClientService
    private val tenantId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        service = ClientService(repo, mock(AuditLogger::class.java))
    }

    @Test
    fun `create and find client`() {
        val saved = service.create(Client(tenantId = tenantId, name = "Taller López"))
        assert(saved.id != null)
        assert(saved.name == "Taller López")

        val found = service.findById(tenantId, saved.id)
        assert(found != null)
        assert(found!!.name == "Taller López")
    }

    @Test
    fun `findAll returns only clients for the given tenant`() {
        val otherTenant = UUID.randomUUID()
        repo.save(Client(tenantId = tenantId, name = "Cliente A"))
        repo.save(Client(tenantId = otherTenant, name = "Cliente B"))

        val clients = service.findAll(tenantId, PageRequest.of(0, 100))
        assert(clients.totalElements == 1L)
    }

    @Test
    fun `findAll filters by name`() {
        repo.save(Client(tenantId = tenantId, name = "Taller Pérez"))
        repo.save(Client(tenantId = tenantId, name = "Herrería García"))
        repo.save(Client(tenantId = tenantId, name = "Taller López"))

        val result = service.findAll(tenantId, PageRequest.of(0, 100), "Taller")
        assert(result.totalElements == 2L)
    }

    @Test
    fun `delete removes client for the correct tenant`() {
        val client = service.create(Client(tenantId = tenantId, name = "Test"))
        val deleted = service.delete(tenantId, client.id)
        assert(deleted)
        assert(repo.findById(client.id).isEmpty)
    }

    @Test
    fun `delete returns false for wrong tenant`() {
        val otherTenant = UUID.randomUUID()
        val client = service.create(Client(tenantId = tenantId, name = "Test"))
        val deleted = service.delete(otherTenant, client.id)
        assert(!deleted)
    }

    @Test
    fun `activate and deactivate client`() {
        val client = service.create(Client(tenantId = tenantId, name = "Test"))
        val deactivated = service.deactivate(tenantId, client.id)
        assert(deactivated!!.status == ClientStatus.INACTIVE)

        val activated = service.activate(tenantId, client.id)
        assert(activated!!.status == ClientStatus.ACTIVE)
    }
}
