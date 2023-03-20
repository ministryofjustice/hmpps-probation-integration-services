package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider.Companion.INTENDED_PROVIDER_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team.Companion.INTENDED_TEAM_CODE

@Entity
@Immutable
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long
) {
    companion object {
        const val INTENDED_PROVIDER_CODE = "CRS"
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
) {
    companion object {
        const val INTENDED_TEAM_CODE = INTENDED_PROVIDER_CODE + "UAT"
    }
}

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    companion object {
        const val INTENDED_STAFF_CODE = INTENDED_TEAM_CODE + "U"
    }
}

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider?
}

fun ProviderRepository.getCrsProvider() =
    findByCode(INTENDED_PROVIDER_CODE) ?: throw NotFoundException("Provider", "code", INTENDED_PROVIDER_CODE)

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}

fun TeamRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("Team", "code", code)

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCode(code: String): Staff?
}

fun StaffRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("Staff", "code", code)
