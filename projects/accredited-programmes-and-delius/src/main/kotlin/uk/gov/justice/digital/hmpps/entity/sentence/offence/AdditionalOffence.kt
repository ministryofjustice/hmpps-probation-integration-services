package uk.gov.justice.digital.hmpps.entity.sentence.offence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @Column(name = "offence_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: OffenceEvent,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: OffenceEntity,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
)
