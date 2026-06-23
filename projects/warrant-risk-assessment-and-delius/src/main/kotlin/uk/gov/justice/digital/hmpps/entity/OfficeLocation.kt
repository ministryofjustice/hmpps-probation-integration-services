package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "office_location")
@SQLRestriction("end_date is null")
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,
    val description: String? = null,
    val buildingName: String? = null,
    val buildingNumber: String? = null,
    val streetName: String? = null,
    val townCity: String? = null,
    val county: String? = null,
    val district: String? = null,
    val postcode: String? = null,
    val endDate: LocalDate? = null,
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findAllByProbationAreaCode(providerCode: String): List<OfficeLocation>
}
