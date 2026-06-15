package com.blacksmith.metalstore.shared

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface NumberSequenceRepository : JpaRepository<NumberSequence, NumberSequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ns FROM NumberSequence ns WHERE ns.id.organizationId = :orgId AND ns.id.prefix = :prefix AND ns.id.year = :year")
    fun findWithLock(@Param("orgId") orgId: UUID, @Param("prefix") prefix: String, @Param("year") year: Int): Optional<NumberSequence>
}
