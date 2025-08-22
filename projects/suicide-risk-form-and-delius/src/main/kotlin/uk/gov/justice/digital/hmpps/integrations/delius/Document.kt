package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "document")
@SQLRestriction("soft_deleted = 0")
class Document(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "alfresco_document_id")
    var alfrescoId: String,

    @Column(name = "document_name")
    var name: String,

    @Column
    val primaryKeyId: Long,

    @Column
    val tableName: String,

    val externalReference: String,

    var lastSaved: ZonedDateTime? = null,

    var lastUpdatedUserId: Long? = null,

    @Column(columnDefinition = "char")
    var workInProgress: String,

    @Column(columnDefinition = "char")
    var status: String,

    @Version
    var rowVersion: Long = 0,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "document_id")
    val id: Long,
) {
    companion object {
        fun suicideRiskFormUrn(uuid: UUID): String =
            "urn:hmpps:suicide-risk-form:$uuid"
    }
}

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByExternalReference(urn: String): Document?

    fun existsByTableNameAndPrimaryKeyIdAndIdNot(tableName: String, primaryKeyId: Long, id: Long): Boolean
}