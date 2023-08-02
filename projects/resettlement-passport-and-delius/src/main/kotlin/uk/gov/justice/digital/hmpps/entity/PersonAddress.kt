package uk.gov.justice.digital.hmpps.entity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Table(name = "offender_address")
@Where(clause = "soft_deleted = 0")
class PersonAddress(
    @Id
    @Column(name = "offender_address_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "address_type_id")
    val type: ReferenceData,
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
    val softDeleted: Boolean = false
)

@Entity
@Immutable
@Table(name = "offender")
@Where(clause = "soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    private val id: Long,

    @Column(columnDefinition = "char(7)")
    private val crn: String,

    val softDeleted: Boolean = false
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {

    @Query(
        """
        select pa from PersonAddress pa
        join fetch pa.status
        join fetch pa.type
        where pa.person.crn = :crn 
        and pa.softDeleted = false  
        and pa.endDate is null 
        and pa.status.code = 'M'
    """
    )
    fun getMainAddressByCrn(crn: String): PersonAddress?
}
