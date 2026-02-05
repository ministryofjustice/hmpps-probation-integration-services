package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.psrUrn

@Entity
@Table(name = "document")
class Document(

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

    @Version
    var rowVersion: Long = 0,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "document_id")
    val id: Long,
) {
    companion object {
        fun psrUrn(uuid: String): String =
            "urn:uk:gov:hmpps:pre-sentence-service:report:${uuid}"
    }
}

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByExternalReference(uuid: String): Document?

    fun getbyUuid(uuid: String): Document {
        return findByExternalReference(psrUrn(uuid))
            ?: throw NotFoundException("Document with external reference ${psrUrn(uuid)} not found")
    }
}