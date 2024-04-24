package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
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

    @Column
    val primaryKeyId: Long,

    @Column(name = "document_name")
    val name: String,

    @Column(name = "document_type")
    val type: String,

    @Column
    val tableName: String,

    @Column(name = "created_datetime")
    val createdAt: ZonedDateTime,

    @Column
    val createdByUserId: Long,

    @Column
    val lastUpdatedUserId: Long,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

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
    val softDeleted: Boolean = false
)
