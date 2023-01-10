package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
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
    val entryLengthUnit: ReferenceData? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
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

    val description: String,
)
