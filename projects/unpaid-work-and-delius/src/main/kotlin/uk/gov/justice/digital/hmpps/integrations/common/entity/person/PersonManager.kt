package uk.gov.justice.digital.hmpps.integrations.common.entity.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.common.entity.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.common.entity.team.Team

@Entity
@Immutable
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id", nullable = false)
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: PersonWithManager,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)
