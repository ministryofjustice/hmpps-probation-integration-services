package uk.gov.justice.digital.hmpps.controller.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class EventEntity(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: CaseEntity,

    @OneToOne(mappedBy = "eventEntity")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,

    @Column(name = "in_breach")
    val inBreach: Boolean = false,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val eventEntity: EventEntity,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", updatable = false)
    val disposalType: DisposalType,

    @OneToMany(mappedBy = "disposal")
    val requirements: List<RequirementEntity>,

    val terminationDate: LocalDate? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "sentence_type")
    val sentenceType: String
)

interface EventRepository : JpaRepository<EventEntity, Long> {

    @Query(
        """
        select e from EventEntity e 
        where e.person.crn = :crn
    """
    )
    fun findByCrn(crn: String): List<EventEntity>
}
