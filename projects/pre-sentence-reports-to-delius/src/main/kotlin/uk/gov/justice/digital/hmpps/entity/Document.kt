package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

@Entity
@Table(name = "document")
@SQLRestriction("soft_deleted = 0")
class Document(
    @Column(name = "document_name")
    var name: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "primary_key_id")
    val courtReport: CourtReport,

    @Column
    val tableName: String,

    @Column
    val externalReference: String,

    @Column(name = "alfresco_document_id")
    var alfrescoId: String,

    @Version
    var rowVersion: Long = 0,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(columnDefinition = "char")
    var workInProgress: String,

    @Column(columnDefinition = "char")
    var status: String,

    var lastUpdatedUserId: Long?,
    var createdDatetime: ZonedDateTime?,
    var lastSaved: ZonedDateTime?,

    @Id
    @Column(name = "document_id")
    val id: Long,
) {
    companion object {
        const val PSR_DOCUMENT_PREFIX = "urn:uk:gov:hmpps:pre-sentence-service:report:"
        fun psrUrn(uuid: String): String =
            "${PSR_DOCUMENT_PREFIX}${uuid}"
    }
}

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByExternalReference(uuid: String): Document?

    fun getByUuid(uuid: String): Document {
        return findByExternalReference(Document.Companion.psrUrn(uuid))
            ?: throw NotFoundException("Document with external reference ${Document.Companion.psrUrn(uuid)} not found")
    }
}