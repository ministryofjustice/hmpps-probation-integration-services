package uk.gov.justice.digital.hmpps.appointments.domain.provider

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "probation_area")
open class Provider(
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "team")
open class Team(
    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

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
@SQLRestriction("end_date is null or end_date > current_date")
open class Location(
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,
    val description: String,

    val endDate: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "staff")
open class Staff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Id
    @Column(name = "staff_id")
    val id: Long,
)

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}

fun TeamRepository.getTeamByCode(code: String) = findByCode(code) ?: throw NotFoundException("Team", "code", code)

interface LocationRepository : JpaRepository<Location, Long> {
    fun findByProviderCodeAndCode(providerCode: String, code: String): Location?
}

fun LocationRepository.getLocationByCode(providerCode: String, code: String) =
    findByProviderCodeAndCode(providerCode, code) ?: throw NotFoundException("Location", "code", code)

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getStaffByCode(code: String) = findByCode(code) ?: throw NotFoundException("Staff", "code", code)