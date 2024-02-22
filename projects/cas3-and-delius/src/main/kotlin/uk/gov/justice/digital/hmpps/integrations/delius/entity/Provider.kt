package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long
) {
    fun homelessPreventionTeamCode() = when (code) {
        "N58" -> "N58XXX"
        "N59" -> "N59N59"
        else -> code + "HPT"
    }
}

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team>,

    @Id
    @Column(name = "staff_id")
    val id: Long
)

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider?
}

fun ProviderRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Provider", "code", code)

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}

fun TeamRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Team", "code", code)
