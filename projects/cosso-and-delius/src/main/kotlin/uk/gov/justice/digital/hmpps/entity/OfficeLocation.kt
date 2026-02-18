package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
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
)

interface OfficeLocationRepository : JpaRepository<OfficeLocation, Long>