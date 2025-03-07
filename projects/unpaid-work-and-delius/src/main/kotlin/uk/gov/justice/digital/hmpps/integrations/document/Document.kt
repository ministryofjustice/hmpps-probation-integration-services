package uk.gov.justice.digital.hmpps.integrations.document

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(name = "document")
@EntityListeners(AuditingEntityListener::class)
class Document(
    @Id
    @SequenceGenerator(name = "document_id_generator", sequenceName = "document_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_id_generator")
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

    @Column(unique = true)
    val externalReference: String,

    @Column(name = "document_name")
    var name: String,

    @CreatedDate
    @Column(nullable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedDate
    var lastSaved: ZonedDateTime? = null,

    @LastModifiedBy
    var lastUpdatedUserId: Long? = null,

    @Version
    var rowVersion: Long = 0,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "char(1)")
    var status: String = "N",
    var sensitive: Boolean = false,
    val partitionAreaId: Long = 0
)
