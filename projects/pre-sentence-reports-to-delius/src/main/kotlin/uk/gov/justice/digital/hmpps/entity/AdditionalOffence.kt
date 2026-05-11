package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "additional_offence")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @Column(name = "offence_date")
    val date: LocalDate?,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToOne
    @JoinColumn(name = "offence_id")
    val offence: OffenceEntity,
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    fun findAllByEventId(eventId: Long): List<AdditionalOffence>
}

