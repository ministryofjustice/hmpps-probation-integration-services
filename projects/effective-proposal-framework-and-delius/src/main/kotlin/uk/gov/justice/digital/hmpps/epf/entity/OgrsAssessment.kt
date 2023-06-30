package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "ogrs_assessment")
@Where(clause = "soft_deleted = 0")
class OgrsAssessment(

    val eventId: Long,

    val assessmentDate: LocalDate,

    @Column(name = "ogrs3_score_2")
    val score: Long?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "ogrs_assessment_id")
    val id: Long
)

interface OgrsAssessmentRepository : JpaRepository<OgrsAssessment, Long> {
    fun findFirstByEventIdOrderByAssessmentDateDesc(eventId: Long): OgrsAssessment?
}
