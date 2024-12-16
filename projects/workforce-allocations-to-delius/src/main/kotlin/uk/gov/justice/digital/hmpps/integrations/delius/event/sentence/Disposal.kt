package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import java.time.ZonedDateTime

@Entity
@Immutable
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "disposal_date")
    val date: ZonedDateTime,

    @Column
    val notionalEndDate: ZonedDateTime? = null,

    @Column
    val enteredNotionalEndDate: ZonedDateTime? = null,

    @Column
    val entryLength: Long? = null,

    @ManyToOne
    @JoinColumn(name = "entry_length_units_id")
    val entryLengthUnit: ReferenceData? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column
    val terminationDate: ZonedDateTime? = null
) {
    val length
        get() = "$entryLength ${entryLengthUnit?.description}"
}

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column
    val sentenceType: String,

    val description: String
)
