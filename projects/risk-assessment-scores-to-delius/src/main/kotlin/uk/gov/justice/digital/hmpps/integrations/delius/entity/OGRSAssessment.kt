package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "ogrs_assessment")
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
class OGRSAssessment(
    @Id
    @Column(name = "ogrs_assessment_id", updatable = false)
    @SequenceGenerator(name = "ogrs_assessment_id_seq", sequenceName = "ogrs_assessment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ogrs_assessment_id_seq")
    val id: Long,
    @Column(name = "assessment_date")
    var assessmentDate: LocalDate,
    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    @OneToOne
    val event: Event,
    @Column(name = "ogrs3_score_1")
    var ogrs3Score1: Long?,
    @Column(name = "ogrs3_score_2")
    var ogrs3Score2: Long?,
    @Column(name = "ogrs2_score")
    val ogrs2Score: Long? = null,
    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,
    @CreatedBy
    var createdByUserId: Long = 0,
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    @CreatedDate
    var createdDatetime: ZonedDateTime? = null,
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime? = null,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Column
    @Version
    val rowVersion: Long = 0L,
)

interface OGRSAssessmentRepository : JpaRepository<OGRSAssessment, Long> {
    fun findByEvent(event: Event): OGRSAssessment?
}
