package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.model.AdditionalSentenceDates
import java.time.LocalDate

@Entity
@Table(name = "additional_sentence")
@SQLRestriction("soft_deleted = 0")
class AdditionalSentence(
    @Id
    @Column(name = "additional_sentence_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)


fun AdditionalSentence.toResponse() = AdditionalSentenceDates(
    eventId = event.id,
    startDate = startDate,
    endDate = endDate
)