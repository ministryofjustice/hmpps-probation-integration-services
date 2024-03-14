package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

@Entity
@Immutable
@Table(name = "personal_contact")
class PersonalContact(
    @Id
    @Column(name = "personal_contact_id")
    val id: Long,

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

interface PersonalContactRepository : JpaRepository<PersonalContact, Long> {
    fun findByPersonId(personId: Long): List<PersonalContact>
}