package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "court_report")
@EntityListeners(AuditingEntityListener::class)
class CourtReport(
    @Id
    @Column(name = "court_report_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "court_appearance_id")
    val courtAppearance: CourtAppearance,

    var completedDate: LocalDate? = null,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long = 0,

    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long = 0,
)

interface CourtReportRepository : JpaRepository<CourtReport, Long>