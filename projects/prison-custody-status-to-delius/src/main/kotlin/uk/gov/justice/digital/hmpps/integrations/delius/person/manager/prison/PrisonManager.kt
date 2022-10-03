package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "prison_offender_manager")
class PrisonManager(
    @Id
    @SequenceGenerator(name = "prison_manager_id_generator", sequenceName = "prison_offender_manager_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prison_manager_id_generator")
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "allocation_date", nullable = false)
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id", nullable = false)
    val allocationReason: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "allocation_team_id", nullable = false)
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    var active: Boolean = true,

    @Column
    var endDate: ZonedDateTime? = null,

    @CreatedBy
    @Column(nullable = false, updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(nullable = false)
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
)
