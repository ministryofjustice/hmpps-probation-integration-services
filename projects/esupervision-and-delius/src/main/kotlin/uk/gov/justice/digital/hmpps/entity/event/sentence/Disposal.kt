package uk.gov.justice.digital.hmpps.entity.event.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column(name = "disposal_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "entry_length")
    val length: Long? = null,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val lengthUnit: ReferenceData? = null,

    @Column(name = "notional_end_date")
    val notionalEndDate: LocalDate? = null,

    @Column(name = "entered_notional_end_date")
    val enteredNotionalEndDate: LocalDate? = null,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    fun expectedEndDate() = enteredNotionalEndDate ?: notionalEndDate
}
