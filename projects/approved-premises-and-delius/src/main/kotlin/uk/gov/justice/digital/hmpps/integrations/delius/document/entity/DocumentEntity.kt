package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "document")
class DocumentEntity(
    @Id
    @Column(name = "document_id")
    val id: Long,

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
    val createdByUserId: Long = 0,

    @Column
    val lastSaved: ZonedDateTime,

    @Column
    val lastUpdatedUserId: Long = 0,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)
