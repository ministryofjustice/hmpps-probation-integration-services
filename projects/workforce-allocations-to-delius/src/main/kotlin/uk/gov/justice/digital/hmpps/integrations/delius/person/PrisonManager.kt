package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team

@Immutable
@Entity
@Table(name = "prison_offender_manager")
class PrisonManager(
    @Id
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,
    @Column(name = "offender_id", nullable = false)
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val provider: Provider,
    @ManyToOne
    @JoinColumn(name = "allocation_team_id", nullable = false)
    val team: Team,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,
)
