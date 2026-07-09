package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import java.time.LocalDate

@Entity
@Table(name = "office_location")
@SQLRestriction("end_date is null")
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,
    val description: String?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val townCity: String?,
    val county: String?,
    val district: String?,
    val postcode: String?,
    val endDate: LocalDate?,
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: ProbationArea,
)

fun OfficeLocation.toAddress() = OfficeAddress(
    id,
    null,
    description,
    buildingName,
    buildingNumber,
    streetName,
    townCity,
    district,
    county,
    postcode,
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findAllByProviderCode(providerCode: String): List<OfficeLocation>
}