package uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
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
    val softDeleted: Boolean,
) : Assessment
