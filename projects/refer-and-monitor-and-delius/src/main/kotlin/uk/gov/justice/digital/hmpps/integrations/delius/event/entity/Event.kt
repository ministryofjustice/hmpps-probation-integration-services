package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

@Entity
@Table(name = "event")
class Event(

    @Column(name = "offender_id")
    val personId: Long,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "ftc_count")
    var ftcCount: Long,

    @Column(name = "in_breach", columnDefinition = "number")
    val inBreach: Boolean,

    @Column(name = "breach_end")
    val breachEnd: LocalDate?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long
)

interface EventRepository : JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = ["disposal.type"])
    override fun findById(id: Long): Optional<Event>
}
