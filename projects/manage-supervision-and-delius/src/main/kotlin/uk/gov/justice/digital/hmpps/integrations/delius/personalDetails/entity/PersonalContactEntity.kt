package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "personal_contact")
@SQLRestriction("soft_deleted = 0")
class PersonalContactEntity(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "other_names")
    val middleNames: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "relationship")
    val relationship: String,

    @Column(name = "email_address")
    val email: String,

    @Column(name = "mobile_number")
    val mobileNumber: String,

    @Column(name = "start_date")
    val startDate: LocalDate?,

    @Column(name = "end_date")
    val endDate: LocalDate?,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    @ManyToOne
    @JoinColumn(name = "relationship_type_id")
    val relationshipType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: ContactAddress,

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id")
    val lastUpdatedUser: User,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface PersonalContactRepository : JpaRepository<PersonalContactEntity, Long> {
    fun findByPersonId(personId: Long): List<PersonalContactEntity>

    @Query(
        """
        select pc from PersonalContactEntity pc where pc.id = :contactId and pc.person.crn = :crn
    """
    )
    fun findById(crn: String, contactId: Long): PersonalContactEntity?

    @Query(
        """
            SELECT pc.FIRST_NAME, pc.OTHER_NAMES, pc.SURNAME, pc.MOBILE_NUMBER, pc.EMAIL_ADDRESS, pc.END_DATE
            FROM PERSONAL_CONTACT pc
            JOIN OFFENDER o
            ON o.OFFENDER_ID = pc.OFFENDER_ID
            JOIN R_STANDARD_REFERENCE_LIST rsrl
            ON rsrl.STANDARD_REFERENCE_LIST_ID = pc.RELATIONSHIP_TYPE_ID 
            JOIN R_LINKED_LIST rll
            ON rll.STANDARD_REFERENCE_DATA1 = pc.RELATIONSHIP_TYPE_ID
            JOIN R_STANDARD_REFERENCE_LIST rsrl2
            ON RSRL2.STANDARD_REFERENCE_LIST_ID = rll.STANDARD_REFERENCE_DATA2
            WHERE o.OFFENDER_ID = :personId
            AND rsrl2.CODE_VALUE = :contactType
    """, nativeQuery = true
    )
    fun getByContactType(personId: Long, contactType: String): List<ContactDetails>
}

fun PersonalContactRepository.getContact(crn: String, id: Long): PersonalContactEntity =
    findById(crn, id) ?: throw NotFoundException("PersonalContact", "id", id)

interface ContactDetails {
    val firstName: String
    val otherNames: String?
    val surname: String
    val mobileNumber: String?
    val emailAddress: String?
    val endDate: LocalDate?
}

