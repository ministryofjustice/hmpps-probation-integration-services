package uk.gov.justice.digital.hmpps.integrations.delius.appointment

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Entity(name = "MsAppointmentProvider")
@Table(name = "probation_area")
class AppointmentProvider(
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,
)

@Immutable
@Entity(name = "MsAppointmentTeam")
@Table(name = "team")
class AppointmentTeam(
    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: AppointmentProvider,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    val description: String,

    @Id
    @Column(name = "team_id")
    val id: Long,
)

@Entity
@Immutable
@Table(name = "all_team", comment = "Legacy team table")
class ProviderTeam(
    @Id
    @Column(name = "trust_provider_team_id")
    val id: Long,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: AppointmentProvider,
) {
    fun toTeam(): AppointmentTeam = AppointmentTeam(provider, code = id.toString(), description, id)
}

@Immutable
@Entity
@Table(name = "office_location")
class AppointmentLocation(
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,
    val description: String,

    val endDate: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: AppointmentProvider,

    @Id
    @Column(name = "office_location_id")
    val id: Long
) {
    fun appointmentNotes(from: AppointmentLocation?): String? =
        if (code != from?.code) {
            val setOrChanged = from?.let {
                "changed from ${it.description}"
            } ?: "set"
            "Location $setOrChanged to $description"
        } else null
}

@Immutable
@Entity(name = "MsAppointmentStaff")
@Table(name = "staff")
class AppointmentStaff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column
    val forename: String,

    @Column
    val surname: String,

    @Id
    @Column(name = "staff_id")
    val id: Long,
)

@Entity
@Immutable
@Table(name = "provider_employee", comment = "Legacy staff table")
class ProviderStaff(
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,

    @Column
    val forename: String,

    @Column
    val surname: String,

    @Id
    @Column(name = "provider_employee_id")
    val id: Long,
) {
    fun toStaff(): AppointmentStaff = AppointmentStaff(code, forename, surname, id)
}

interface AppointmentTeamRepository : JpaRepository<AppointmentTeam, Long> {
    fun findByCode(code: String): AppointmentTeam?
}

fun AppointmentTeamRepository.getByCode(code: String): AppointmentTeam =
    findByCode(code) ?: throw NotFoundException("Team", "code", code)

interface AppointmentLocationRepository : JpaRepository<AppointmentLocation, Long> {
    fun findByCodeAndProviderCodeAndEndDateIsNull(code: String, providerCode: String): AppointmentLocation?
}

fun AppointmentLocationRepository.getByCode(providerCode: String, code: String): AppointmentLocation =
    findByCodeAndProviderCodeAndEndDateIsNull(code, providerCode) ?: throw NotFoundException("Location", "code", code)

interface AppointmentStaffRepository : JpaRepository<AppointmentStaff, Long> {
    fun findByCode(code: String): AppointmentStaff?
}

fun AppointmentStaffRepository.getByCode(code: String): AppointmentStaff =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)