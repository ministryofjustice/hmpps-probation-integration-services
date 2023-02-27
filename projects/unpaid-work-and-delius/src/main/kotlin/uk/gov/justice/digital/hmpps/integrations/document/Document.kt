package uk.gov.justice.digital.hmpps.integrations.document

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(name = "document")
@EntityListeners(AuditingEntityListener::class)
class Document(
    @Id
    @Column(name = "document_id")
    val id: Long = 0,

    @Column(name = "primary_key_id")
    val contactId: Long,

    @Column(name = "offender_id")
    val offenderId: Long,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column(name = "table_name")
    val tableName: String,

    val externalReference: String,

    @Column(name = "document_name")
    var name: String,

    @LastModifiedDate
    var lastSaved: ZonedDateTime? = null,

    @LastModifiedBy
    var lastUpdatedUserId: Long? = null,

    @Version
    var rowVersion: Long? = null,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "char(1)")
    var status: String = "N",
    var sensitive: Boolean = false,
    val partitionAreaId: Long = 0
)
