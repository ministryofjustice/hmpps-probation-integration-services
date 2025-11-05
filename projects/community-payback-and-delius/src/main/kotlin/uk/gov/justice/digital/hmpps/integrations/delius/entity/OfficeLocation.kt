package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Table(name = "office_location")
@Immutable
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,

    val buildingName: String?,

    val buildingNumber: String?,

    val streetName: String?,

    @Column(name = "town_city")
    val town: String?,

    val county: String?,

    val postcode: String?,
)