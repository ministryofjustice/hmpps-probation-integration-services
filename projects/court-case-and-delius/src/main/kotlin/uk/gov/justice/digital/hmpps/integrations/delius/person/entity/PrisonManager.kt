package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Manager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.ZonedDateTime

@Entity
@Table(name = "prison_offender_manager")
class PrisonManager(

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "allocation_date", nullable = false)
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id", nullable = false)
    val allocationReason: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    override val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "allocation_team_id", nullable = false)
    override val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    override val probationArea: ProbationAreaEntity,

    @Column(name = "telephone_number")
    val telephoneNumber: String?,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "prisonManager")
    val responsibleOfficers: List<ResponsibleOfficer> = emptyList(),

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,

    ) : Manager {
    @Column
    var endDate: ZonedDateTime? = null

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    var active: Boolean = true

    @CreatedBy
    @Column(nullable = false, updatable = false)
    var createdByUserId: Long = 0

    @LastModifiedBy
    @Column(nullable = false)
    var lastUpdatedUserId: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    var emailAddress: String? = null

    fun isUnallocated() = staff.code.endsWith("U")

    fun responsibleOfficer(): ResponsibleOfficer? = responsibleOfficers.firstOrNull { it.isActive() }
}

@Entity
@Table(name = "responsible_officer")
class ResponsibleOfficer(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "PRISON_OFFENDER_MANAGER_ID")
    var prisonManager: PrisonManager?,

    val startDate: ZonedDateTime,

    val endDate: ZonedDateTime? = null,

    @Id
    @Column(name = "responsible_officer_id", nullable = false)
    val id: Long = 0
) {

    fun isActive() = endDate == null
}
