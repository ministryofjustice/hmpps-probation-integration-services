package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.jpa.GeneratedId
import java.time.ZonedDateTime
import java.util.*

@Entity
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
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

    @LastModifiedDate
    var lastSaved: ZonedDateTime? = ZonedDateTime.now(),

    @CreatedDate
    var createdDatetime: ZonedDateTime? = ZonedDateTime.now(),

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null,

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastUpdatedUserId: Long? = null,

    @Column(columnDefinition = "char")
    var workInProgress: String,

    @Column(columnDefinition = "char")
    var status: String,

    @Version
    var rowVersion: Long = 0,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "partition_area_id")
    val partitionAreaId: Long = 0,

    @Id
    @SequenceGenerator(name = "document_id_generator", sequenceName = "document_id_seq", allocationSize = 1)
    @GeneratedId(generator = "document_id_generator")
    @Column(name = "document_id")
    val id: Long = 0,
) {
    companion object {
        const val COMMUNITY_PAYBACK_URN_PREFIX: String = "urn:hmpps:community-payback:appointment-document:"

        fun communityPaybackUrn(uuid: UUID): String = "$COMMUNITY_PAYBACK_URN_PREFIX$uuid"
    }
}

interface DocumentRepository : JpaRepository<Document, Long> {
    fun existsByTableNameAndPrimaryKeyIdAndIdNot(tableName: String, primaryKeyId: Long, id: Long): Boolean

    fun findAllByPrimaryKeyIdAndTableName(primaryKeyId: Long, tableName: String = "CONTACT"): List<Document>
}
