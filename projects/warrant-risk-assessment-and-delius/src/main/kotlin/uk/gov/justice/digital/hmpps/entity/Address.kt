package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class Address(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,

    @Column(name = "building_name")
    val buildingName: String? = null,

    @Column(name = "address_number")
    val buildingNumber: String? = null,

    @Column(name = "street_name")
    val streetName: String? = null,

    @Column(name = "town_city")
    val townCity: String? = null,

    val district: String? = null,

    val county: String? = null,

    val postcode: String? = null,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByPersonIdAndEndDateIsNull(personId: Long): List<Address>
}
