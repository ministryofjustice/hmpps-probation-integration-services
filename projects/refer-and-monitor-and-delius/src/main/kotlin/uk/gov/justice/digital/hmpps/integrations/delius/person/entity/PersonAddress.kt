package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0 and end_date is null")
class PersonAddress(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,

    @Column(name = "building_name")
    val buildingName: String?,
    @Column(name = "address_number")
    val addressNumber: String?,
    @Column(name = "street_name")
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,

    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_address_id")
    val id: Long
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @EntityGraph(attributePaths = ["status"])
    fun findByPersonIdAndStatusCode(personId: Long, statusCode: String): List<PersonAddress>
}

fun PersonAddressRepository.mainAddress(personId: Long) = findByPersonIdAndStatusCode(personId, "M").firstOrNull()
