package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff
import java.time.LocalDate

@Entity
@Immutable
class OffenderManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team?,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff?,

    val endDate: LocalDate?
)

@Immutable
@Entity(name = "professional_contact_team")
@Table(name = "team")
class Team(
    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    val description: String,

    @Id
    @Column(name = "team_id")
    val id: Long
)