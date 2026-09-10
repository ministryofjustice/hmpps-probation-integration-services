package uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@Table(name = "main_offence")
@SQLRestriction("soft_deleted = 0")
class MainOffenceWithSa2026Exclusions(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: EventWithSa2026Exclusions? = null,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: OffenceWithSa2026Exclusions,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
)
