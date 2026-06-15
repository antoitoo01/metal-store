package com.blacksmith.metalstore.shared

import jakarta.persistence.*
import java.io.Serializable
import java.util.Objects
import java.util.UUID

@Embeddable
data class NumberSequenceId(
    val organizationId: UUID,
    val prefix: String,
    val year: Int
) : Serializable

@Entity
@Table(name = "number_sequences")
class NumberSequence(
    @EmbeddedId
    val id: NumberSequenceId,

    @Column(nullable = false)
    var counter: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as NumberSequence
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
