package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "office_location")
@SQLRestriction("end_date is null or end_date > current_date")
class OfficeLocation(

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

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findAllByProviderCode(providerCode: String): List<OfficeLocation>
}