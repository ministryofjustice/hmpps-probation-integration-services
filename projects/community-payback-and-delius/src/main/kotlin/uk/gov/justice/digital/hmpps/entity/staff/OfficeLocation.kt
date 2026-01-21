package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "office_location")
@Immutable
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val code: String,

    val buildingName: String?,

    val buildingNumber: String?,

    val streetName: String?,

    @Column(name = "town_city")
    val town: String?,

    val county: String?,

    val postcode: String?,
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long> {
    fun findAllByCodeIn(codes: List<String>): List<OfficeLocation>
}