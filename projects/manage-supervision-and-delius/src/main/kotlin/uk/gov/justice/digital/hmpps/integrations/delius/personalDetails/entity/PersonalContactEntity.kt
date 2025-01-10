package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
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
    @Convert(converter = NumericBooleanConverter::class)
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
}

fun PersonalContactRepository.getContact(crn: String, id: Long): PersonalContactEntity =
    findById(crn, id) ?: throw NotFoundException("PersonalContact", "id", id)

