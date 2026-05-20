package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.utils.Extensions.reportMissing
import java.time.ZonedDateTime

@Entity
@Table(name = "office_location")
@Immutable
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val code: String,
    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime?,
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    @Query(
        """
        select ol from OfficeLocation ol 
        where ol.code in (:codes)
          and (ol.endDate is null or ol.endDate > current_date) 
          and ol.provider.selectable = true
        """
    )
    fun findByCodeIn(codes: Collection<String>): List<OfficeLocation>

    @Query(
        """
        select ol from OfficeLocation ol 
        where ol.code = :code
          and (ol.endDate is null or ol.endDate > current_date) 
          and ol.provider.selectable = true
        """
    )
    fun findByCode(code: String): OfficeLocation?

    fun getByCode(code: String): OfficeLocation = findByCode(code).orNotFoundBy("code", code)

    fun getByCodeIn(codes: List<String>) = codes.toSet().let { codes ->
        findByCodeIn(codes).associateBy { it.code }.reportMissing(codes)
    }

    @Query(
        """
        select ol from OfficeLocation ol
        join TeamOfficeLocation tol on tol.officeLocationId = ol.id
        where tol.teamId = :teamId
        and (ol.endDate is null or ol.endDate > current_date)
        and ol.provider.selectable = true
        """
    )
    fun getLocationsByTeam(teamId: Long): List<OfficeLocation>
}