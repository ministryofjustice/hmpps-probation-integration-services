package uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "ogrs_assessment")
@SQLRestriction("soft_deleted = 0")
class OGRSAssessment(
    @Id
    @Column(name = "ogrs_assessment_id")
    val id: Long,

    @Column(name = "assessment_date")
    override val assessmentDate: LocalDate,

    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    @OneToOne
    val event: Event,

    @Column(name = "ogrs3_score_2")
    override val score: Long,

    @Column(name = "last_updated_datetime", nullable = false)
    override val lastModifiedDateTime: ZonedDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean

) : Assessment
