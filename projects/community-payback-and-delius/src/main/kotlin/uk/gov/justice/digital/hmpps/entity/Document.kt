package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.person.Person
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "document")
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

    @Column(name = "table_name")
    val tableName: String,

    val externalReference: String,

    var lastSaved: ZonedDateTime? = null,

    var createdDatetime: ZonedDateTime? = null,

    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null,

    @Column(name = "last_updated_user_id")
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
    @SequenceGenerator(name = "document_id_generator", sequenceName = "document_id_seq", allocationSize = 1)
    @uk.gov.justice.digital.hmpps.jpa.GeneratedId(generator = "document_id_generator")
    @Column(name = "document_id")
    val id: Long = 0,
) {
    companion object {
        const val COMMUNITY_PAYBACK_URN_PREFIX: String = "urn:hmpps:community-payback:appointment-document:"

        fun communityPaybackUrn(uuid: UUID): String = "$COMMUNITY_PAYBACK_URN_PREFIX$uuid"
    }
}

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByExternalReferenceAndSoftDeletedFalse(urn: String): Document?

    fun existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse(
        tableName: String,
        primaryKeyId: Long,
        id: Long
    ): Boolean
}
