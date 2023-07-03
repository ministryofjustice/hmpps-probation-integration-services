package uk.gov.justice.digital.hmpps.integrations.delius.manager.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "responsible_officer")
class ResponsibleOfficer(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager,

    val startDate: ZonedDateTime = ZonedDateTime.now(),

    val endDate: ZonedDateTime? = null,

    @Id
    @Column(name = "responsible_officer_id")
    private val id: Long
)

@Immutable
@Entity
@Table(name = "offender_manager")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)
