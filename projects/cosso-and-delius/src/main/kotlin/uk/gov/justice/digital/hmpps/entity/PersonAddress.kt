package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonAddress(
    @Id
    @Column(name = "offender_address_id")
    val id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    val buildingName: String? = null,
    val addressNumber: String? = null,
    val streetName: String? = null,
    val townCity: String? = null,
    val district: String? = null,
    val county: String? = null,
    val postcode: String? = null,
    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData,
    @Column(name = "end_date")
    val endDate: LocalDate? = null,
    @Column(columnDefinition = "number", name = "soft_deleted")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @Query("SELECT a FROM PersonAddress a WHERE a.person.offenderId = :personId")
    fun findByPersonId(personId: Long): List<PersonAddress>
}
