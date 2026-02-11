package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Table(name = "offender_address")
@SQLRestriction("soft_deleted = 0")
class PersonAddress(
    @Id
    @Column("offender_address_id")
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
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

interface PersonAddressRepository : JpaRepository<PersonAddress, Long> {
    @Query("SELECT a FROM PersonAddress a WHERE a.person.offenderId = :personId")
    fun findByPersonId(personId: Long): List<PersonAddress>
}
