package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "document")
@EntityListeners(AuditingEntityListener::class)
class Document(
  @Id
  @Column(name = "document_id")
  val id: Long = 0,

  @Column(name = "primary_key_id")
  val courtReportId: Long,

  @Column(name = "alfresco_document_id")
  val alfrescoId: String,

  val externalReference: String,

  var documentName: String,

  @LastModifiedDate
  var lastSaved: ZonedDateTime,

  @LastModifiedBy
  var lastUpdatedUserId: Long,

  @Version
  var rowVersion: Long,
)
