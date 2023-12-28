package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider.Companion.INTENDED_PROVIDER_CODE
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    val description: String,

    val endDate: LocalDate?,

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

    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @Column(name = "email_address")
    val email: String?,
    val telephone: String?,

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

    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val startDate: LocalDate,
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
    @Query(
        """
        select l from Location l
        join fetch l.provider p
        where l.code = :code
        and (p.endDate is null or p.endDate > current_date)
        and (l.endDate is null or l.endDate > current_date)
        """
    )
    fun findActiveLocationByCode(code: String): Location?

    @Query(
        """
        select l from Location l
        join fetch l.provider p
        where p.id = :providerId 
        and (p.endDate is null or p.endDate > current_date)
        and (l.endDate is null or l.endDate > current_date)
    """
    )
    fun findAllLocationsForProvider(providerId: Long): List<Location>
}

fun LocationRepository.getByCode(code: String) =
    findActiveLocationByCode(code) ?: throw NotFoundException("Location", "code", code)
