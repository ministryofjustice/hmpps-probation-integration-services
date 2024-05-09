package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "borough")
class ProbationDeliveryUnit(
    @Id
    @Column(name = "borough_id")
    val id: Long,
    val code: String,
    val description: String,
)

@Entity
@Immutable
@Table(name = "district")
class LocalAdminUnit(
    @Id
    @Column(name = "district_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val pdu: ProbationDeliveryUnit,
)

@Entity
@Immutable
@Table(name = "team")
class TeamWithLocalAdminUnit(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "probation_area_id")
    val providerId: Long,

    val description: String,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,

    @ManyToMany(mappedBy = "teams")
    val staff: List<StaffWithUser> = listOf(),

    @ManyToOne
    @JoinColumn(name = "district_id")
    val localAdminUnit: LocalAdminUnit? = null,
)

fun Team.withLocalAdminUnit(localAdminUnit: LocalAdminUnit) =
    TeamWithLocalAdminUnit(id, code, providerId, description, endDate, staff, localAdminUnit)

fun TeamWithLocalAdminUnit.toTeam() = Team(id, code, providerId, description, endDate, staff)