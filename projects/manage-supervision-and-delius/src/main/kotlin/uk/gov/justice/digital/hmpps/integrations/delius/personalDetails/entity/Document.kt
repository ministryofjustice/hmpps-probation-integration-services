package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "document")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "table_name", discriminatorType = DiscriminatorType.STRING)
@SQLRestriction("soft_deleted = 0")
abstract class Document {
    @Id
    @Column(name = "document_id")
    open var id: Long = 0

    @Column(name = "offender_id")
    open var personId: Long = 0

    @Column(name = "primary_key_id")
    open var primaryKeyId: Long? = null

    @Column(name = "alfresco_document_id")
    open var alfrescoId: String = ""

    @Column(name = "document_name")
    open var name: String = ""

    @Column(name = "document_type")
    open var type: String = ""

    @Column(name = "last_saved")
    open var lastUpdated: ZonedDateTime = ZonedDateTime.now()

    @Column(columnDefinition = "number")
    open var softDeleted: Boolean = false
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("CONTACT")
class ContactDocument(
    @JoinColumn(name = "primary_key_id", referencedColumnName = "contact_id", insertable = false, updatable = false)
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    val contact: Contact?
) : Document()

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("OFFENDER")
class PersonDocument : Document()

interface DocumentRepository : JpaRepository<PersonDocument, Long> {
    fun findByPersonId(personId: Long): List<PersonDocument>

    @Query("select d.name from PersonDocument d join Person p on p.id = d.personId and p.crn = :crn and d.alfrescoId = :alfrescoId")
    fun findNameByPersonCrnAndAlfrescoId(crn: String, alfrescoId: String): String?
}

fun DocumentRepository.getDocument(crn: String, alfrescoId: String) =
    findNameByPersonCrnAndAlfrescoId(crn, alfrescoId) ?: throw NotFoundException("Document", "alfrescoId", alfrescoId)

