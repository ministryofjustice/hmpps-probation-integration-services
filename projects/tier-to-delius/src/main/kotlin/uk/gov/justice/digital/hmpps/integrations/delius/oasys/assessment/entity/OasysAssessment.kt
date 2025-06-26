package uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.Assessment
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "oasys_assessment")
@SQLRestriction("soft_deleted = 0")
class OasysAssessment(
    @Id
    @Column(name = "oasys_assessment_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "assessment_date")
    override val assessmentDate: LocalDate,

    @Column(name = "ogrs_score_2")
    override val score: Long?,

    @Column(name = "last_updated_datetime", nullable = false)
    override val lastModifiedDateTime: ZonedDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean

) : Assessment