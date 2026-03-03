package uk.gov.justice.digital.hmpps.entity.event.offence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.event.EventEntity

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
