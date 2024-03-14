package uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

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
    fun findNameByPersonCrnAndAlfrescoId(crn: String, alfrescoId: String): String?
}

fun DocumentRepository.getDocument(crn: String, alfrescoId: String) =
    findNameByPersonCrnAndAlfrescoId(crn, alfrescoId) ?: throw NotFoundException("Document", "alfrescoId", alfrescoId)

