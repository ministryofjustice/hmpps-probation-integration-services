package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
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
    @OneToOne
    @JoinColumn(name = "offence_id")
    val offences: Offence
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    fun findAllByEventId(eventId: Long): List<AdditionalOffence>
}

