package uk.gov.justice.digital.hmpps.integrations.delius.document

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import java.time.ZonedDateTime

@Entity
@Table(name = "document")
class Document(
    @Id
    @Column(name = "document_id")
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "primary_key_id")
    val courtReportId: Long,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    val externalReference: String,

    val templateName: String,

    val tableName: String = "COURT_REPORT",
    val documentType: String = "DOCUMENT",

    @Column(name = "document_name")
    var name: String,

    val createdByUserId: Long,
    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    var lastSaved: ZonedDateTime? = null,

    var lastUpdatedUserId: Long? = null,

    @Column(name = "last_upd_author_provider_id")
    var lastUpdatedProviderId: Long?,

    @Column(name = "created_provider_id")
    var createdProviderId: Long?,

    @Version
    var rowVersion: Long = 0,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "partition_area_id")
    private val partitionAreaId: Long = 0
)
