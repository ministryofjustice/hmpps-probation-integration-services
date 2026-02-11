package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "additional_offence")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val additionalOffenceId: Long,
    val offenceDate: LocalDate,
    val eventId: Long,
    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: OffenceEntity
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    fun findAllByEventId(eventId: Long): List<AdditionalOffence>
}