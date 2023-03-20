package uk.gov.justice.digital.hmpps.integrations.delius.referral.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "disposal")
class Disposal(
    @Column(name = "offender_id")
    val personId: Long,
    val eventId: Long,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

interface DisposalRepository : JpaRepository<Disposal, Long> {
    fun findByPersonIdAndEventId(personId: Long, eventId: Long): Disposal?
}

fun DisposalRepository.getByPersonIdAndEventId(personId: Long, eventId: Long) =
    findByPersonIdAndEventId(personId, eventId) ?: throw NotFoundException("Event", "id", eventId)
