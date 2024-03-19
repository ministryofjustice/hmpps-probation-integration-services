package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable

@Entity
@Table(name = "offender_prisoner")
class Prisoner(
    @EmbeddedId
    val id: PrisonerId,

    val partitionAreaId: Long = 0,

    @Column(name = "row_version")
    val version: Long = 0,
)

@Embeddable
class PrisonerId(
    @Column(name = "offender_id")
    val personId: Long,

    val prisonerNumber: String,
) : Serializable

interface PrisonerRepository : JpaRepository<Prisoner, PrisonerId> {
    fun deleteAllByIdPersonId(personId: Long)
}

