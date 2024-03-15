package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Entity
@Immutable
@Table(name = "personal_contact")
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

    @ManyToOne
    @JoinColumn(name = "relationship_type_id")
    val relationshipType: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "address_id")
    val address: ContactAddress,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,
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