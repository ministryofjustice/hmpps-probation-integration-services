package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Immutable
@Entity
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
