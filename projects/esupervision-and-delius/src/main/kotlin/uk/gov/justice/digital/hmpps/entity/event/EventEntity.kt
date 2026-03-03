package uk.gov.justice.digital.hmpps.entity.event

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.event.offence.MainOffence
import uk.gov.justice.digital.hmpps.entity.event.sentence.Disposal
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class EventEntity(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column
    val referralDate: LocalDate,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface EventRepository : JpaRepository<EventEntity, Long> {
    @EntityGraph(attributePaths = ["disposal.type", "mainOffence.offence"])
    fun findFirstByPersonCrnOrderByReferralDateDesc(crn: String): EventEntity?
}
