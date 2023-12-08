package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.ZonedDateTime

@Entity
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val provider: ProbationAreaEntity,
    @Column(name = "allocation_date")
    val date: ZonedDateTime,
    @Column(name = "active_flag", columnDefinition = "NUMBER")
    val active: Boolean = true,
    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false,
)
