package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff

interface EventOffence {
    val offence: Offence
    val event: Event
    val softDeleted: Boolean
}

@Immutable
@Entity
@Table(name = "main_offence")
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    override val offence: Offence,

    @OneToOne
    @JoinColumn(name = "event_id")
    override val event: Event,

    @Column(updatable = false, columnDefinition = "NUMBER")
    override val softDeleted: Boolean = false
) : EventOffence

@Immutable
@Entity
@Table(name = "additional_offence")
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    override val offence: Offence,

    @OneToOne
    @JoinColumn(name = "event_id")
    override val event: Event,

    @Column(updatable = false, columnDefinition = "NUMBER")
    override val softDeleted: Boolean = false
) : EventOffence

@Immutable
@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    val description: String
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    @EntityGraph(attributePaths = ["offence", "event"])
    fun findAllByEventIdInAndSoftDeletedFalse(eventIds: List<Long>): List<AdditionalOffence>
}

data class SentenceWithManager(
    val disposal: Disposal,
    val mainOffence: MainOffence,
    val manager: Staff
)
