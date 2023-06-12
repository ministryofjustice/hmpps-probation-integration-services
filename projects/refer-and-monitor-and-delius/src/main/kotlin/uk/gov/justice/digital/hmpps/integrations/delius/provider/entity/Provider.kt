package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider.Companion.INTENDED_PROVIDER_CODE

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    val description: String,

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

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

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
@Table(name = "office_location")
class Location(
    @Column(name = "code", columnDefinition = "char(7)")
    val code: String,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "district")
class District(

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "borough_id")
    val id: Long
)

interface ProviderRepository : JpaRepository<Provider, Long> {
    fun findByCode(code: String): Provider?
}

fun ProviderRepository.getCrsProvider() =
    findByCode(INTENDED_PROVIDER_CODE) ?: throw NotFoundException("Provider", "code", INTENDED_PROVIDER_CODE)

interface TeamRepository : JpaRepository<Team, Long> {
    @EntityGraph(attributePaths = ["district.borough"])
    fun findByCode(code: String): Team?
}

fun TeamRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("Team", "code", code)

interface LocationRepository : JpaRepository<Location, Long> {
    fun findByCode(code: String): Location?
}

fun LocationRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException("Location", "code", code)
