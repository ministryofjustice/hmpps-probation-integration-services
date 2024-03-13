package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

interface PersonalDetailsRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["gender", "religion", "personalContacts"])
    fun findByCrn(crn: String): Person?
}

@Immutable
@Entity(name = "PersonalDetailsAddress")
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0 and end_date is null")
class PersonAddress(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData,

    @Column(name = "building_name")
    val buildingName: String?,
    @Column(name = "address_number")
    val buildingNumber: String?,
    @Column(name = "street_name")
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,

    val startDate: LocalDate,
    val endDate: LocalDate? = null,

    @Column(name = "last_updated_timestamp")
    val lastUpdated: LocalDate?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_address_id")
    val id: Long
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @EntityGraph(attributePaths = ["status", "type"])
    fun findByPersonId(personId: Long): List<PersonAddress>
}

fun PersonalDetailsRepository.getPersonDetails(crn: String) =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)


