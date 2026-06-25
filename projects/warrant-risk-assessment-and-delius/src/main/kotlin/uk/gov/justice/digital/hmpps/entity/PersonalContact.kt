package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "personal_contact")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonalContact(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "other_names")
    val middleNames: String? = null,

    @Column(name = "surname")
    val surname: String,

    @ManyToOne
    @JoinColumn(name = "relationship_type_id")
    val relationshipType: ReferenceData,

    @Column(name = "mobile_number")
    val mobileNumber: String? = null,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: ContactAddress? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

@Entity
@Immutable
@Table(name = "address")
@SQLRestriction("soft_deleted = 0")
class ContactAddress(
    @Id
    @Column(name = "address_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "address_status_id")
    val status: ReferenceData? = null,

    val buildingName: String? = null,

    @Column(name = "address_number")
    val buildingNumber: String? = null,

    val streetName: String? = null,

    @Column(name = "town_city")
    val townCity: String? = null,

    val district: String? = null,

    val county: String? = null,

    val postcode: String? = null,

    val telephoneNumber: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface PersonalContactRepository : JpaRepository<PersonalContact, Long> {
    @Query(
        """
        select pc from PersonalContact pc
        where pc.personId = :personId
        and pc.relationshipType.code = 'CE'
        and pc.endDate is null
        """
    )
    fun findCurrentEmployersByPersonId(personId: Long): List<PersonalContact>
}
