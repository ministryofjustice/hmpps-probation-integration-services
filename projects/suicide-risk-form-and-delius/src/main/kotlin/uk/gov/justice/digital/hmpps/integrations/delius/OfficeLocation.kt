package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "office_location")
@SQLRestriction("end_date is null or end_date > current_date")
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,
    val description: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val townCity: String?,
    val county: String?,
    val postcode: String?,
    val endDate: LocalDate?,
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,
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

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findAllByProviderCode(providerCode: String): List<OfficeLocation>
}