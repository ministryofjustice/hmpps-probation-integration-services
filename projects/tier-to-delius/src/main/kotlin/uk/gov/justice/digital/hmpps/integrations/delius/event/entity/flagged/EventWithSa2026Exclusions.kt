package uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class EventWithSa2026Exclusions(
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

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffenceWithSa2026Exclusions,

    @OneToMany(mappedBy = "event")
    val additionalOffences: List<AdditionalOffenceWithSa2026Exclusions>,

    @Column(name = "in_breach")
    val inBreach: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

