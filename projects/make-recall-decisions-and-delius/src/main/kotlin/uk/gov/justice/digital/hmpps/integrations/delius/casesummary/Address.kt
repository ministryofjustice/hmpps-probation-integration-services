package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Immutable
@Table(name = "offender_address")
@Entity(name = "CaseSummaryAddress")
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

    @Column
    val buildingName: String?,

    @Column
    val addressNumber: String?,

    @Column
    val streetName: String?,

    @Column(name = "town_city")
    val town: String?,

    @Column
    val district: String?,

    @Column
    val county: String?,

    @Column
    val postcode: String?,

    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface CaseSummaryAddressRepository : JpaRepository<Address, Long> {
    @EntityGraph(attributePaths = ["status"])
    fun findAddressByPersonIdAndStatusCode(personId: Long, statusCode: String): Address?
}

fun CaseSummaryAddressRepository.findMainAddress(personId: Long) = findAddressByPersonIdAndStatusCode(personId, "M")
