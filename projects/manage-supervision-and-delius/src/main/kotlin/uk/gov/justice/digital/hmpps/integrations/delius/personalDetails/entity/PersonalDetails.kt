package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonalDetails
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.awt.ComponentOrientation
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity(name = "PersonalDetails")
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class PersonDetails(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "preferred_name", length = 35)
    val preferredName: String?,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "telephone_number")
    val telephoneNumber: String?,

    @Column(name = "mobile_number")
    val mobileNumber: String?,

    @Column(name = "e_mail_address")
    val emailAddress: String?,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "sexual_orientation_id")
    val sexualOrientation: ReferenceData?,

    @OneToMany(mappedBy = "personId")
    val personalCircumstances: List<PersonalCircumstance>,

    @OneToMany(mappedBy = "personId")
    val disabilities: List<Disability>,

    @OneToMany(mappedBy = "personId")
    val provisions: List<Provision>,

    @OneToMany(mappedBy = "personId")
    val personalContacts: List<PersonalContact>,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

)

interface PersonalDetailsRepository : JpaRepository<PersonDetails, Long> {
    @EntityGraph(attributePaths = ["gender", "ethnicity", "nationality", "religion", "genderIdentity", "personalContacts"])
    fun findByCrn(crn: String): PersonDetails?
}

@Immutable
@Entity(name = "PersonalDetailsCircumstance")
@Table(name = "personal_circumstance")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "circumstance_type_id")
    val type: ReferenceData,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "circumstance_sub_type_id")
    val subType: PersonalCircumstanceSubType,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    val startDate: LocalDate,

    val endDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

@Immutable
@Entity(name = "PersonalDetailsCircumstanceSubtype")
@Table(name = "r_circumstance_sub_type")
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    val id: Long,

    @Column(name = "code_description")
    val description: String,
)

@Immutable
@Entity(name = "PersonalDetailsDisability")
@Table(name = "disability")
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Disability(
    @Id
    @Column(name = "disability_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "disability_type_id")
    val type: ReferenceData,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    val startDate: LocalDate,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

@Immutable
@Entity
@Table(name = "PersonalDetailsProvision")
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Provision(
    @Id
    @Column(name = "provision_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "provision_type_id")
    val type: ReferenceData,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    val startDate: LocalDate,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

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

fun PersonalDetailsRepository.getPersonDetails(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

@Entity
@Immutable
@Table(name = "document")
@SQLRestriction("soft_deleted = 0")
class PersonDocument(
    @Id
    @Column(name = "document_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column
    val primaryKeyId: Long,

    @Column(name = "document_name")
    val name: String,

    @Column(name = "document_type")
    val type: String,

    @Column
    val tableName: String,

    @Column(name = "last_saved")
    val lastUpdated: ZonedDateTime,

    @Column(name = "created_datetime")
    val createdAt: ZonedDateTime,

    @Column
    val createdByUserId: Long = 0,

    @Column
    val lastUpdatedUserId: Long = 0,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface DocumentRepository : JpaRepository<PersonDocument, Long> {
    fun findByPersonId(personId: Long): List<PersonDocument>

    @Query("select d.name from PersonDocument d join Person p on p.id = d.personId and p.crn = :crn and d.alfrescoId = :alfrescoId")
    fun findNameByPersonCrnAndAlfrescoId(crn: String, id: String)  : String?
}

@Entity
@Immutable
@Table(name = "personal_contact")
class PersonalContact(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long,

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

    @Column(name = "notes")
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

