package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "offender")
class DocPerson(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "CHAR(7)")
    val crn: String,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "prev_conviction_document_name")
    val preconDocName: String?,

    @Column(name = "prev_con_alfresco_document_id")
    val preconDocId: String?,

    @Column(name = "prev_con_created_datetime")
    val preconDocCreatedDate: ZonedDateTime?
)
