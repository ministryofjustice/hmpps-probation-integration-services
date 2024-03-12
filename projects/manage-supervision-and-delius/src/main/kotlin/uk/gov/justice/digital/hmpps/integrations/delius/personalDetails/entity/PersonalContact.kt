package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "personal_contact")
class PersonalContact(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long,

    @Id
    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "other_names")
    val middleNames: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "relationship")
    val relationship: String,

    @ManyToOne
    @JoinColumn(name = "relationship_type_id")
    val relationshipType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: ContactAddress,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,
)

@Immutable
@Entity
@Table(name = "address")
@SQLRestriction("soft_deleted = 0")
class ContactAddress(
    @Id
    @Column(name = "address_id")
    val id: Long,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    @Column(name = "town_city")
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)