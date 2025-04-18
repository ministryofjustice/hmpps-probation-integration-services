package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class PersonAddress(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    var status: ReferenceData,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    @Convert(converter = YesNoConverter::class)
    val noFixedAbode: Boolean? = false,
    var endDate: LocalDate? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {

    @Query(
        """
        select pa from PersonAddress pa
        join fetch pa.status
        where pa.person.id = :personId 
        and pa.softDeleted = false  
        and pa.endDate is null 
        and pa.status.code = 'M'
    """
    )
    fun getMainAddressByPersonId(personId: Long): PersonAddress?
}

interface PersonRepository : JpaRepository<Person, Long> {
    @Query("select p.crn from Person p where p.noms = :nomsId")
    fun findCrnByNomsId(nomsId: String): String?

    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)