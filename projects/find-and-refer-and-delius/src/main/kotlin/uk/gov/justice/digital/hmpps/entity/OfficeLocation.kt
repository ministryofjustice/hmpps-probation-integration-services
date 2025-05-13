package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.annotation.Immutable
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "office_location")
class OfficeLocation(

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

    @JoinColumn(name = "district_id")
    @ManyToOne
    val ldu: District,

    @Id
    @Column(name = "office_location_id")
    val id: Long
)

fun OfficeLocation.asAddress(email: String? = null) = OfficeAddress.from(
    description,
    buildingName,
    buildingNumber,
    streetName,
    district,
    townCity,
    county,
    postcode,
    email,
    ldu.description,
    telephoneNumber,
    startDate,
    endDate
)