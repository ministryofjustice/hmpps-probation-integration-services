package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import java.time.LocalDate
import kotlin.jvm.Transient

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
    val staff: Staff,

    val endDate: LocalDate?
)

@Immutable
@Entity(name = "contact_staff")
@Table(name = "staff")
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long,

    val forename: String,
    val surname: String,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser?
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Column(name = "distinguished_name")
    val username: String,

    ) {
    @Transient
    var email: String? = null

    @Transient
    var telephone: String? = null
}

@Immutable
@Entity(name = "professional_contact_team")
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    val description: String,
)

