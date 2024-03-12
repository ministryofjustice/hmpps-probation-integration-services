package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity(name = "PersonDetails")
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
    @EntityGraph(attributePaths = ["gender", "religion", "personalContacts"])
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
@Entity(name = "PersonalDetailsProvision")
@Table(name = "provision")
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

fun PersonalDetailsRepository.getPersonDetails(crn: String) =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)


