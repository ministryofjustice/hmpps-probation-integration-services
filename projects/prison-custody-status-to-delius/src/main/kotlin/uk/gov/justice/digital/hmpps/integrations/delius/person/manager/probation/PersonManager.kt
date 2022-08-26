package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "offender_manager")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id", nullable = false)
    val id: Long,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id", nullable = false)
    val allocationReason: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)
