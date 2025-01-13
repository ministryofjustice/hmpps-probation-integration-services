package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.Instant
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "document")
@SQLRestriction("soft_deleted = 0")
class DocumentEntity(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "alfresco_document_id")
    val alfrescoId: String,

    @Column(name = "primary_key_id")
    val primaryKeyId: Long,

    @Column(name = "document_name")
    val name: String,

    @Column(name = "document_type")
    val type: String,

    @Column(name = "table_name")
    val tableName: String?,

    @Column(name = "template_name")
    val templateName: String?,

    @Column(name = "created_datetime")
    val createdAt: ZonedDateTime,

    @Column(name = "last_saved")
    val lastSaved: ZonedDateTime,

    @Column(name = "created_by_user_id")
    val createdByUserId: Long,

    @Column(name = "last_updated_user_id")
    val lastUpdatedUserId: Long,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "document_id")
    val id: Long,
)

interface Document {
    val alfrescoId: String
    val name: String
    val type: String
    val tableName: String
    val createdAt: Instant?
    val lastUpdatedAt: Instant?
    val author: String?
    val description: String?
    val eventId: Long?
}

@Immutable
@Table(name = "offender")
@Entity(name = "DocumentPerson")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
