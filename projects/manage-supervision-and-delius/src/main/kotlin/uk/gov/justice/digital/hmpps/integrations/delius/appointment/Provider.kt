package uk.gov.justice.digital.hmpps.integrations.delius.appointment

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "probation_area")
class AppointmentProvider(
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,
)

@Immutable
@Entity
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

@Immutable
@Entity
@Table(name = "office_location")
class AppointmentLocation(
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,
    val description: String,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "staff")
class AppointmentStaff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Id
    @Column(name = "staff_id")
    val id: Long,
)

interface AppointmentTeamRepository : JpaRepository<AppointmentTeam, Long> {
    fun findByCode(code: String): AppointmentTeam?
}

fun AppointmentTeamRepository.getByCode(code: String): AppointmentTeam =
    findByCode(code) ?: throw NotFoundException("Team", "code", code)

interface AppointmentLocationRepository : JpaRepository<AppointmentLocation, Long> {
    fun findByCode(code: String): AppointmentLocation?
}

fun AppointmentLocationRepository.getByCode(code: String): AppointmentLocation =
    findByCode(code) ?: throw NotFoundException("Location", "code", code)

interface AppointmentStaffRepository : JpaRepository<AppointmentStaff, Long> {
    fun findByCode(code: String): AppointmentStaff?
}

fun AppointmentStaffRepository.getByCode(code: String): AppointmentStaff =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)